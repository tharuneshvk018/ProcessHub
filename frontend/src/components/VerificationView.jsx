import React, { useState } from 'react';
import { 
  ShieldCheck, 
  CheckCircle2, 
  RotateCcw, 
  AlertTriangle, 
  Clock, 
  FileCheck, 
  Image, 
  User, 
  Lock,
  ArrowRight
} from 'lucide-react';

export default function VerificationView({ 
  issues, 
  currentRole, 
  currentUser, 
  onVerifyResolution, 
  onSelectIssue 
}) {
  const [selectedIssue, setSelectedIssue] = useState(null);
  const [verificationType, setVerificationType] = useState(null); // 'APPROVE', 'REOPEN'
  const [verificationNotes, setVerificationNotes] = useState('');

  // Issues pending independent verification
  const pendingVerificationIssues = issues.filter(i => 
    i.status === 'RESOLVED_PENDING_VERIFICATION'
  );

  const isTechnician = currentRole === 'ROLE_TECHNICIAN';

  const handleOpenVerification = (issue, type) => {
    setSelectedIssue(issue);
    setVerificationType(type);
    setVerificationNotes(type === 'APPROVE' 
      ? 'Site inspected. Operational criteria met under full load conditions. Sign-off granted.' 
      : 'Rework required: Secondary leak still present at fitting joint.');
  };

  const handleConfirmVerification = () => {
    if (!selectedIssue) return;

    if (!verificationNotes || verificationNotes.trim().length < 5) {
      alert('Please provide verification sign-off notes.');
      return;
    }

    onVerifyResolution(
      selectedIssue.id, 
      verificationType === 'APPROVE', 
      verificationNotes
    );

    setVerificationType(null);
    setSelectedIssue(null);
  };

  return (
    <div className="view-container">
      {/* Separation of Duties Banner */}
      <div className="glass-panel" style={{ 
        padding: '18px 24px', 
        marginBottom: '24px', 
        background: isTechnician ? 'rgba(239, 68, 68, 0.08)' : 'rgba(16, 185, 129, 0.08)',
        border: isTechnician ? '1px solid rgba(239, 68, 68, 0.3)' : '1px solid rgba(16, 185, 129, 0.3)',
        borderRadius: 'var(--radius-lg)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          {isTechnician ? (
            <Lock size={22} color="var(--rose-500)" />
          ) : (
            <ShieldCheck size={22} color="var(--emerald-500)" />
          )}
          <div>
            <h4 style={{ fontSize: '0.95rem', fontWeight: 700, color: '#FFF' }}>
              Separation of Duties Policy Enforcement
            </h4>
            <p style={{ fontSize: '0.84rem', color: isTechnician ? '#FCA5A5' : '#6EE7B7' }}>
              {isTechnician 
                ? 'Active Role is TECHNICIAN (Bob Builder). By system policy, the technician who performed the repair cannot independently sign off and close their own work.'
                : 'Active Role is SUPERVISOR / REPORTER. You are authorized to independently inspect and verify completed work.'}
            </p>
          </div>
        </div>
      </div>

      {/* Cards list */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
        {pendingVerificationIssues.length === 0 ? (
          <div className="glass-panel" style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
            No resolved issues currently pending independent sign-off.
          </div>
        ) : (
          pendingVerificationIssues.map(issue => (
            <div 
              key={issue.id}
              className="glass-panel"
              style={{ padding: '24px', position: 'relative' }}
            >
              <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: '14px', marginBottom: '16px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                    <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: '#A5B4FC' }}>
                      {issue.referenceCode}
                    </span>
                    <span className={`priority-chip ${issue.priority}`}>
                      {issue.priority}
                    </span>
                    <span className="status-badge RESOLVED_PENDING_VERIFICATION">
                      Awaiting Independent Sign-off
                    </span>
                  </div>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-primary)' }}>
                    {issue.title}
                  </h3>
                  <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                    {issue.locationBuilding}, {issue.locationRoom} &bull; {issue.departmentName}
                  </div>
                </div>

                {/* Verification Actions */}
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                  {isTechnician ? (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.82rem', color: '#F87171', background: 'rgba(239, 68, 68, 0.1)', padding: '6px 14px', borderRadius: 'var(--radius-md)' }}>
                      <Lock size={14} />
                      <span>Verification Disabled (Technician Self-Signoff Prohibited)</span>
                    </div>
                  ) : (
                    <>
                      <button 
                        className="btn-success"
                        onClick={() => handleOpenVerification(issue, 'APPROVE')}
                      >
                        <CheckCircle2 size={15} />
                        <span>Sign Off & Close</span>
                      </button>

                      <button 
                        className="btn-danger"
                        onClick={() => handleOpenVerification(issue, 'REOPEN')}
                      >
                        <RotateCcw size={15} />
                        <span>Reject & Reopen</span>
                      </button>
                    </>
                  )}

                  <button 
                    className="btn-secondary" 
                    onClick={() => onSelectIssue(issue)}
                  >
                    <span>Full Audit</span>
                    <ArrowRight size={13} />
                  </button>
                </div>
              </div>

              {/* Resolution Evidence Box */}
              <div style={{ 
                background: 'rgba(15, 23, 42, 0.6)', 
                border: '1px solid var(--border-subtle)', 
                borderRadius: 'var(--radius-md)', 
                padding: '16px',
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
                gap: '16px'
              }}>
                <div>
                  <div style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '4px' }}>
                    Work Summary by Technician:
                  </div>
                  <p style={{ fontSize: '0.9rem', color: '#FFF', lineHeight: 1.4 }}>
                    {issue.workSummary || 'Repair completed according to operational procedure.'}
                  </p>
                  
                  {issue.rootCause && (
                    <div style={{ marginTop: '10px' }}>
                      <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)' }}>Root Cause: </span>
                      <span style={{ fontSize: '0.84rem', color: '#A5B4FC' }}>{issue.rootCause}</span>
                    </div>
                  )}

                  <div style={{ display: 'flex', gap: '16px', marginTop: '10px', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                    <span>Labor: <strong>{issue.laborHours || 2.0} hrs</strong></span>
                    <span>Parts: <strong>{issue.partsUsed || 'Standard inventory items'}</strong></span>
                  </div>
                </div>

                {issue.resolutionEvidenceUrl && (
                  <div>
                    <div style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '6px' }}>
                      Completion Evidence Photo:
                    </div>
                    <img 
                      src={issue.resolutionEvidenceUrl} 
                      alt="Repair completion evidence"
                      style={{ width: '100%', maxHeight: '140px', objectFit: 'cover', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}
                    />
                  </div>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      {/* Verification Modal */}
      {verificationType && selectedIssue && (
        <div className="modal-backdrop">
          <div className="modal-dialog">
            <div className="modal-header">
              <span style={{ fontWeight: 700, fontSize: '1.05rem' }}>
                {verificationType === 'APPROVE' 
                  ? `Sign-Off & Close Ticket: ${selectedIssue.referenceCode}`
                  : `Reject Verification & Reopen: ${selectedIssue.referenceCode}`}
              </span>
              <button onClick={() => setVerificationType(null)} style={{ background: 'transparent', color: 'var(--text-muted)' }}>✕</button>
            </div>

            <div className="modal-body">
              <div className="blocker-alert" style={{ marginBottom: '16px' }}>
                <ShieldCheck size={16} />
                <span>
                  {verificationType === 'APPROVE'
                    ? 'Signing off will permanently close this ticket and append an immutable VERIFICATION_PASSED event.'
                    : 'Rejection transitions ticket back to IN_PROGRESS and assigns rework directly to the technician.'}
                </span>
              </div>

              <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                {verificationType === 'APPROVE' ? 'Inspector Verification Notes:' : 'Rework Failure Reason & Rework Directive:'}
              </label>
              <textarea 
                rows={4} 
                value={verificationNotes}
                onChange={e => setVerificationNotes(e.target.value)}
              />
            </div>

            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setVerificationType(null)}>Cancel</button>
              <button 
                className={verificationType === 'APPROVE' ? 'btn-success' : 'btn-danger'}
                onClick={handleConfirmVerification}
              >
                {verificationType === 'APPROVE' ? 'Confirm Sign-Off & Close' : 'Reopen for Rework'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
