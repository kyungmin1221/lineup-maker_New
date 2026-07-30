import { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import Header from '../components/Header';
import QuarterTabs from '../components/QuarterTabs';
import FormationChips from '../components/FormationChips';
import Pitch from '../components/Pitch';
import AnimBar from '../components/AnimBar';
import ScenarioTabs from '../components/ScenarioTabs';
import Bench from '../components/Bench';
import Comments from '../components/Comments';
import Toast from '../components/Toast';
import { useLineup } from '../hooks/useLineup';
import { useToast } from '../hooks/useToast';
import {
  getLineup,
  updateLineup,
  addComment as saveComment,
  deleteComment as removeComment,
  subscribeToLineup,
  getOrCreateEditToken,
} from '../api/lineupApi';
import { findMyLockerRooms, getLockerRoom } from '../api/lockerRoomApi';
import { getDeviceId } from '../api/deviceId';
import { trackEvent } from '../lib/analytics';
import { C, nextId } from '../constants';
import { share, getTossShareLink } from '@apps-in-toss/web-framework';

const CACHE_KEY = 'lineup-maker:my-lineup-id';

// 옛 nextId 버그로 인한 squad 중복 ID 자동 복구
function dedupeSquadIds(squad) {
  if (!Array.isArray(squad)) return { squad, changed: false };
  const seen = new Set();
  let changed = false;
  const cleaned = squad.map((p) => {
    if (seen.has(p.id)) {
      changed = true;
      return { ...p, id: nextId() };
    }
    seen.add(p.id);
    return p;
  });
  return { squad: cleaned, changed };
}

export default function CreatePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const urlToken = searchParams.get('token');
  const [initialData, setInitialData] = useState(null);
  const [loadError, setLoadError] = useState(false);
  const [isOwner, setIsOwner] = useState(false);

  // 진입 시 device id 확보 + 라인업 로드
  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const uid = getDeviceId();
        const data = await getLineup(id);
        if (cancelled) return;
        if (!data) {
          setLoadError(true);
          return;
        }
        const ownerMatch = data.ownerId === uid;
        // 서버는 editToken을 응답에 내려주지 않으므로(api-spec.md 참고, 보안상 비노출),
        // 토큰이 실제로 유효한지는 여기서 검증할 수 없다 - URL에 토큰이 있으면 일단 진입을
        // 허용하고, 실제 저장(PATCH) 시점에 서버가 403으로 최종 판단한다.
        const hasToken = !!urlToken;
        // 소유자도 아니고 토큰도 없으면 진입 차단
        if (!ownerMatch && !hasToken) {
          navigate('/', { replace: true });
          return;
        }
        if (ownerMatch) {
          // squad 중복 ID 복구 (있을 경우 서버에도 즉시 반영)
          const dedup = dedupeSquadIds(data.squad);
          if (dedup.changed) {
            data.squad = dedup.squad;
            await updateLineup(id, { squad: dedup.squad }, { deviceId: uid }).catch(console.error);
          }
          setIsOwner(true);
        }
        localStorage.setItem(CACHE_KEY, id);
        setInitialData({ ...data, _uid: uid });
      } catch (err) {
        console.error(err);
        if (!cancelled) setLoadError(true);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [id, navigate, urlToken]);

  if (loadError) {
    return (
      <div style={{ background: C.bg, minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: 48, fontWeight: 800, color: C.blueLight, marginBottom: 8 }}>404</div>
          <div style={{ fontSize: 14, color: C.muted }}>라인업을 찾을 수 없습니다.</div>
        </div>
      </div>
    );
  }

  if (!initialData) {
    return (
      <div style={{ background: C.bg, minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{ fontSize: 14, color: C.muted }}>불러오는 중...</div>
      </div>
    );
  }

  return (
    <Editor
      id={id}
      initialData={initialData}
      isOwner={isOwner}
      uid={initialData._uid}
      editToken={urlToken}
    />
  );
}

function Editor({ id, initialData, isOwner, uid, editToken }) {
  const [editingTeam, setEditingTeam] = useState(false);
  const [showLockerModal, setShowLockerModal] = useState(false);
  const [showOpponents, setShowOpponents] = useState(initialData?.showOpponents ?? true);
  const { toast, showToast } = useToast();

  // onSnapshot이 트리거한 상태 변경에서 자동저장이 다시 실행되는 것을 방지
  const skipNextSave = useRef(false);

  const {
    teamName, setTeamName,
    squad, quarters, activeIdx, setActiveIdx,
    phase, setPhase,
    scenarios, activeScenarioIdx, switchScenario,
    animStepIdx, setAnimStepIdx, animSteps,
    quarter, bench, displayPlayers, displayOpponents, displayBall,
    addToPitch, removeFromPitch, dragPlayer, setPlayerLabel, applyFormation,
    deleteFromSquad, addPlayer,
    addQuarter, removeQuarter,
    initAnimSteps, addAnimStep, removeAnimStep,
    addScenario, removeScenario, renameScenario,
    dragOpponent, dragBall,
    addComment, deleteComment, syncRemoteComments,
  } = useLineup(initialData);

  const [isPlaying, setIsPlaying] = useState(false);

  const handleSetPhase = useCallback((newPhase) => {
    if (newPhase === 'move') initAnimSteps();
    setIsPlaying(false);
    setPhase(newPhase);
  }, [initAnimSteps, setPhase]);

  // 애니메이션 타이머
  useEffect(() => {
    if (!isPlaying || phase !== 'move') return;
    if (animSteps.length < 2) { setIsPlaying(false); return; }
    const timer = setInterval(() => {
      setAnimStepIdx((prev) => {
        const next = prev + 1;
        if (next >= animSteps.length) { setIsPlaying(false); return 0; }
        return next;
      });
    }, 1200);
    return () => clearInterval(timer);
  }, [isPlaying, phase, animSteps.length, setAnimStepIdx]);

  // 친구 댓글을 실시간으로 동기화
  useEffect(() => {
    const unsub = subscribeToLineup(id, (remote) => {
      if (remote?.quarters) {
        skipNextSave.current = true;
        syncRemoteComments(remote.quarters);
      }
    });
    return unsub;
  }, [id, syncRemoteComments]);

  // 라인업 변경 시 1초 후 자동 저장
  useEffect(() => {
    if (skipNextSave.current) {
      skipNextSave.current = false;
      return;
    }
    const timer = setTimeout(() => {
      updateLineup(id, { teamName, squad, quarters, showOpponents }, { deviceId: uid, editToken })
        .catch((err) => {
          console.error(err);
          if (err.status === 403) {
            showToast('저장 권한이 없어요. 편집 링크를 다시 확인해주세요.');
          }
        });
    }, 1000);
    return () => clearTimeout(timer);
  }, [id, teamName, squad, quarters, showOpponents, uid, editToken]);

  const isTossApp = window.location.hostname.includes('tossmini.com');

  // 편집 링크 공유 (소유자 전용)
  const handleShareEdit = async () => {
    try {
      const token = await getOrCreateEditToken(id, uid);
      if (isTossApp) {
        const tossLink = await getTossShareLink(`intoss://lineupmaker/edit/${id}?token=${token}`, 'https://lineup-maker-tau.vercel.app/og-image.png');
        await share({ message: `${teamName} 라인업 편집\n${tossLink}` });
        trackEvent('share_edit_link', { method: 'toss' });
      } else {
        const webLink = `${window.location.origin}/edit/${id}?token=${token}`;
        await navigator.clipboard.writeText(webLink);
        showToast('편집 링크가 복사됐어요!');
        trackEvent('share_edit_link', { method: 'clipboard' });
      }
    } catch (err) {
      if (err?.name === 'AbortError') return;
      console.error(err);
      showToast('오류가 발생했습니다.');
    }
  };

  // 공유 버튼: 뷰 링크 공유
  const handleShare = async () => {
    try {
      if (isTossApp) {
        const tossLink = await getTossShareLink(`intoss://lineupmaker/view/${id}`, 'https://lineup-maker-tau.vercel.app/og-image.png');
        await share({ message: `${teamName} 라인업\n${tossLink}` });
        trackEvent('share_lineup', { method: 'toss' });
      } else {
        const webLink = `${window.location.origin}/view/${id}`;
        await navigator.clipboard.writeText(webLink);
        showToast('링크가 복사됐어요!');
        trackEvent('share_lineup', { method: 'clipboard' });
      }
    } catch (err) {
      if (err?.name === 'AbortError') return;
      console.error(err);
      showToast('오류가 발생했습니다.');
    }
  };

  // 목록(findMyLockerRooms)엔 playerCount만 있고 실제 players는 없어서, 선택 시점에 단건 조회로 다시 받아온다
  const handleImportFromLockerRoom = async (room) => {
    setShowLockerModal(false);
    let fullRoom;
    try {
      fullRoom = await getLockerRoom(room.id);
    } catch (err) {
      console.error(err);
      showToast('라커룸을 불러오지 못했어요.');
      return;
    }
    if (!fullRoom || fullRoom.players.length === 0) {
      showToast('선수가 없는 라커룸이에요.');
      return;
    }
    if (bench.length > 0) {
      const ok = window.confirm(
        `"${fullRoom.name || '라커룸'}"의 선수 ${fullRoom.players.length}명을 불러올게요.\n\n확인: 기존 대기선수를 지우고 교체\n취소: 기존 선수 유지하고 추가`
      );
      if (ok) bench.forEach(p => deleteFromSquad(p.id));
    }
    fullRoom.players.forEach(p => addPlayer(p.name, p.number || '-'));
    showToast(`${fullRoom.players.length}명을 대기선수로 불러왔어요!`);
  };

  // 작성자 댓글: 로컬 + Firestore 동시 저장
  const handleAddComment = async (name, text) => {
    addComment(name, text);
    await saveComment(id, activeIdx, { name, text }).catch(console.error);
    trackEvent('add_comment', { role: 'owner' });
  };

  const handleDeleteComment = async (commentIdx) => {
    deleteComment(commentIdx);
    await removeComment(id, activeIdx, commentIdx, uid).catch(console.error);
  };

  return (
    <div style={{ background: C.bg, minHeight: '100%', color: C.text }}>
      <div style={{ maxWidth: 480, margin: '0 auto' }}>
        <Header
          teamName={teamName} setTeamName={setTeamName}
          editingTeam={editingTeam} setEditingTeam={setEditingTeam}
          onShare={handleShare} readOnly={false}
          onShareEdit={isOwner ? handleShareEdit : undefined}
        />

        <QuarterTabs
          quarters={quarters} activeIdx={activeIdx} setActiveIdx={setActiveIdx}
          addQuarter={addQuarter} removeQuarter={removeQuarter} readOnly={false}
        />

        {phase !== 'move' && (
          <FormationChips
            activeFormation={quarter.formations?.base}
            onApply={applyFormation}
          />
        )}

        {phase === 'move' && (
          <>
            <ScenarioTabs
              scenarios={scenarios}
              activeIdx={activeScenarioIdx}
              onSwitch={(idx) => { switchScenario(idx); setIsPlaying(false); initAnimSteps(); }}
              onAdd={() => { addScenario(); setIsPlaying(false); }}
              onRemove={removeScenario}
              onRename={renameScenario}
            />
            <AnimBar
              steps={animSteps}
              activeIdx={animStepIdx}
              onSetIdx={setAnimStepIdx}
              isPlaying={isPlaying}
              onTogglePlay={() => setIsPlaying((p) => !p)}
              onAddStep={addAnimStep}
              onRemoveStep={removeAnimStep}
              readOnly={false}
            />
          </>
        )}

        <Pitch
          placedPlayers={displayPlayers} squad={squad}
          onDrag={dragPlayer} onRemove={removeFromPitch}
          onLabelChange={setPlayerLabel}
          readOnly={false}
          phase={phase} setPhase={handleSetPhase}
          formation={quarter.formations?.[phase]}
          opponents={displayOpponents}
          ball={displayBall}
          onDragOpponent={dragOpponent}
          onDragBall={dragBall}
          showOpponents={showOpponents}
          onToggleOpponents={() => {
            const next = !showOpponents;
            setShowOpponents(next);
            updateLineup(id, { showOpponents: next }, { deviceId: uid, editToken }).catch(console.error);
          }}
        />

        <div style={{ margin: '20px 24px 0', height: 1, background: C.border }} />

        {/* 라커룸에서 불러오기 */}
        <div style={{ padding: '16px 24px 0', display: 'flex', justifyContent: 'flex-end' }}>
          <button
            onClick={() => setShowLockerModal(true)}
            style={{
              fontSize: 12, fontWeight: 600, color: C.sub,
              background: 'transparent', border: `1px solid ${C.border}`,
              borderRadius: 99, padding: '5px 12px',
              cursor: 'pointer', transition: 'border-color 0.15s, color 0.15s',
            }}
            onMouseEnter={e => { e.currentTarget.style.borderColor = C.borderMid; e.currentTarget.style.color = C.text; }}
            onMouseLeave={e => { e.currentTarget.style.borderColor = C.border; e.currentTarget.style.color = C.sub; }}
          >
            라커룸에서 불러오기
          </button>
        </div>

        <Bench
          bench={bench} onAddToPitch={addToPitch}
          onDeleteFromSquad={deleteFromSquad} onAddPlayer={addPlayer} readOnly={false}
        />

        <div style={{ margin: '20px 24px 0', height: 1, background: C.border }} />

        <Comments
          quarter={quarter}
          onAddComment={handleAddComment}
          canDelete
          onDeleteComment={handleDeleteComment}
        />
      </div>

      <Toast message={toast} />

      {showLockerModal && (
        <LockerRoomModal
          uid={uid}
          onImport={handleImportFromLockerRoom}
          onClose={() => setShowLockerModal(false)}
        />
      )}
    </div>
  );
}

function LockerRoomModal({ uid, onImport, onClose }) {
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!uid) { setLoading(false); return; }
    findMyLockerRooms(uid)
      .then(r => setRooms(r))
      .catch(err => console.error('라커룸 목록 로드 실패:', err))
      .finally(() => setLoading(false));
  }, [uid]);

  return (
    <div
      onClick={onClose}
      style={{
        position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        zIndex: 200, padding: '0 24px',
      }}
    >
      <div
        onClick={e => e.stopPropagation()}
        style={{
          width: '100%', maxWidth: 400,
          background: C.surface,
          borderRadius: 20,
          padding: '28px 24px',
        }}
      >
        <p style={{ fontSize: 16, fontWeight: 700, color: C.text, margin: '0 0 16px' }}>
          라커룸에서 불러오기
        </p>
        {loading ? (
          <p style={{ fontSize: 14, color: C.muted }}>불러오는 중...</p>
        ) : rooms.length === 0 ? (
          <p style={{ fontSize: 14, color: C.muted }}>저장된 라커룸이 없어요. 먼저 라커룸을 만들어보세요.</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {rooms.map(r => (
              <button
                key={r.id}
                onClick={() => onImport(r)}
                style={{
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                  background: C.raised, border: `1px solid ${C.border}`,
                  borderRadius: 12, padding: '14px 18px',
                  color: C.text, fontSize: 15, fontWeight: 600,
                  cursor: 'pointer', textAlign: 'left',
                  transition: 'border-color 0.15s',
                }}
                onMouseEnter={e => e.currentTarget.style.borderColor = C.borderMid}
                onMouseLeave={e => e.currentTarget.style.borderColor = C.border}
              >
                <span>{r.name || '이름 없는 라커룸'}</span>
                <span style={{ fontSize: 13, color: C.muted, fontWeight: 500 }}>{r.playerCount}명</span>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
