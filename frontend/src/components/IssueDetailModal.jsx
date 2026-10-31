import React, { useState } from 'react';
import { 
  X, 
  MapPin, 
  Cpu, 
  Calendar, 
  Clock, 
  User, 
  ShieldAlert, 
  AlertTriangle, 
  DollarSign, 
  FileText, 
  History,
  CheckCircle2,
  ExternalLink
} from 'lucide-react';

export default function IssueDetailModal({ 
  issue, 
  onClose,
  onProvideInfo,
  currentRole
}) {
  const [rfiAnswer, setRfiAnswer] = useState('');
  const [activeTab, setActiveTab] = useState('TIMELINE'); // 'TIMELINE', 'EXPENDITURES', 'EVIDENCE'

  if (!issue) return null;

  const isMoreInfoRequested = issue.status === 'MORE_INFO_REQUESTED';
  const recurrence = issue.recurrenceIntelligence;

  const handleSendRfiResponse = () => {
    if (!rfiAnswer.trim() || rfiAnswer.trim().length < 5) {
      alert('Please provide details in response to the RFI.');
      return;
    }
    onProvideInfo(issue.id, rfiAnswer);
    setRfiAnswer('');
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div 
        className="modal-dialog" 
        onClick={e => e.stopPropagation()}
        style={{ maxWidth: '820px' }}
      >
        {/* Header */}
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 800, color: 'var(--indigo-600)', fontSize: '1.2rem' }}>
              {issue.referenceCode}
            </span>
            <span className={`priority-chip ${issue.priority}`}>
              {issue.priority}
            </span>
            <span className={`status-badge ${issue.status}`}>
              {issue.status.replace(/_/g, ' ')}
            </span>
          </div>

          <button 
            onClick={onClose} 
            style={{ background: 'transparent', color: 'var(--text-muted)' }}
          >
            <X size={20} />
          </button>
        </div>

        {/* Body */}
        <div className="modal-body">
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '8px', color: 'var(--text-primary)' }}>
            {issue.title}
          </h2>
          <p style={{ fontSize: '0.92rem', color: 'var(--text-secondary)', lineHeight: 1.5, marginBottom: '16px' }}>
            {issue.description}
          </p>

          {/* Explainable Workflow Progress Box */}
          <div className="explainable-progress-box">
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '10px' }}>
              <span style={{ fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--indigo-600)', letterSpacing: '0.05em' }}>
                Explainable Workflow Progress Engine
              </span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.78rem', color: issue.slaStatus === 'BREACHED' ? '#DC2626' : '#059669', fontWeight: 600 }}>
                <Clock size={13} />
                <span>SLA: {issue.slaStatus || 'ON_TRACK'}</span>
              </div>
            </div>

            <div className="progress-banner-grid">
              <div className="progress-banner-item">
                <span className="progress-item-label">Current Lifecycle Stage</span>
                <span className="progress-item-value">{issue.currentStage || issue.status}</span>
              </div>
              <div className="progress-banner-item">
                <span className="progress-item-label">Responsible Role / Team</span>
                <span className="progress-item-value" style={{ color: 'var(--indigo-600)' }}>{issue.responsibleRole || 'Unassigned'}</span>
              </div>
              <div className="progress-banner-item">
                <span className="progress-item-label">Next Required Action</span>
                <span className="progress-item-value" style={{ fontSize: '0.86rem' }}>{issue.nextAction || 'Pending triage'}</span>
              </div>
            </div>

            {issue.blockingReason && (
              <div className="blocker-alert">
                <AlertTriangle size={16} />
                <span><strong>Active Blocker:</strong> {issue.blockingReason}</span>
              </div>
            )}
          </div>

          {/* Recurrence Intelligence Alert */}
          {recurrence && recurrence.chronic && (
            <div className={`recurrence-alert-card ${recurrence.recurrenceLevel}`}>
              <ShieldAlert size={22} style={{ flexShrink: 0, marginTop: '2px' }} />
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                  <strong style={{ fontSize: '0.92rem' }}>Recurring-Issue Intelligence Warning:</strong>
                  <span className="priority-chip CRITICAL">{recurrence.recurrenceLevel}</span>
                </div>
                <p style={{ fontSize: '0.84rem', lineHeight: 1.4, marginBottom: '8px' }}>
                  {recurrence.recommendation}
                </p>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                  Prior correlated incidents: {recurrence.relatedIssues.map(r => r.referenceCode).join(', ')}
                </div>
              </div>
            </div>
          )}

          {/* Reporter RFI Response Box */}
          {isMoreInfoRequested && (
            <div style={{ background: '#FFFBEB', border: '1px solid #FDE68A', borderRadius: 'var(--radius-md)', padding: '16px', marginBottom: '18px' }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#B45309', marginBottom: '6px' }}>
                Action Required: Supervisor Requested More Information
              </div>
              <p style={{ fontSize: '0.84rem', color: 'var(--text-secondary)', marginBottom: '10px' }}>
                Please clarify the details so triage and assignment can proceed.
              </p>
              <textarea 
                rows={3} 
                placeholder="Type your response to the supervisor's inquiry here..."
                value={rfiAnswer}
                onChange={e => setRfiAnswer(e.target.value)}
                style={{ marginBottom: '10px' }}
              />
              <button className="btn-primary" onClick={handleSendRfiResponse}>
                Submit Information & Unblock Review
              </button>
            </div>
          )}

          {/* Details Metadata Grid */}
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
            gap: '12px', 
            padding: '14px', 
            background: 'var(--bg-surface)', 
            borderRadius: 'var(--radius-md)',
            marginBottom: '20px',
            fontSize: '0.84rem'
          }}>
            <div>
              <span style={{ color: 'var(--text-muted)' }}>Location: </span>
              <strong>{issue.locationBuilding}, {issue.locationRoom}</strong>
            </div>
            {issue.assetId && (
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Asset Tag: </span>
                <strong style={{ color: 'var(--cyan-500)', fontFamily: 'var(--font-mono)' }}>{issue.assetId}</strong>
              </div>
            )}
            <div>
              <span style={{ color: 'var(--text-muted)' }}>Department: </span>
              <strong>{issue.departmentName}</strong>
            </div>
            <div>
              <span style={{ color: 'var(--text-muted)' }}>Assigned Tech: </span>
              <strong>{issue.assignedTechnicianName || 'Unassigned'}</strong>
            </div>
          </div>

          {/* Tabs for Timeline / Expenditures / Evidence */}
          <div style={{ display: 'flex', gap: '8px', borderBottom: '1px solid var(--border-subtle)', marginBottom: '16px' }}>
            <button 
              className="btn-secondary" 
              style={{ borderRadius: '0', borderBottom: activeTab === 'TIMELINE' ? '2px solid var(--indigo-500)' : 'none', background: 'transparent' }}
              onClick={() => setActiveTab('TIMELINE')}
            >
              <History size={15} />
              <span>Immutable Audit Timeline</span>
            </button>
            <button 
              className="btn-secondary" 
              style={{ borderRadius: '0', borderBottom: activeTab === 'EXPENDITURES' ? '2px solid var(--indigo-500)' : 'none', background: 'transparent' }}
              onClick={() => setActiveTab('EXPENDITURES')}
            >
              <DollarSign size={15} />
              <span>Expenditures ({(issue.expenditures || []).length})</span>
            </button>
          </div>

          {/* Timeline View */}
          {activeTab === 'TIMELINE' && (
            <div className="timeline-list">
              {(issue.timeline || []).map((event, idx) => (
                <div key={event.id || idx} className="timeline-item">
                  <div className="timeline-dot" />
                  <div className="timeline-content">
                    <div className="timeline-header">
                      <span className="timeline-actor">{event.actorName || 'System'}</span>
                      <span className="timeline-time">
                        {new Date(event.timestamp).toLocaleString()}
                      </span>
                    </div>
                    <div style={{ fontSize: '0.78rem', fontWeight: 600, color: 'var(--indigo-500)', marginBottom: '4px' }}>
                      {event.action}
                    </div>
                    <div className="timeline-notes">
                      {event.notes}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Expenditures View */}
          {activeTab === 'EXPENDITURES' && (
            <div>
              {(issue.expenditures || []).length === 0 ? (
                <div style={{ textAlign: 'center', padding: '24px', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  No expenditures requested for this issue.
                </div>
              ) : (
                issue.expenditures.map(exp => (
                  <div key={exp.id} style={{ background: 'var(--bg-surface)', padding: '14px', borderRadius: 'var(--radius-md)', marginBottom: '10px', border: '1px solid var(--border-subtle)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                      <span style={{ fontWeight: 700, color: '#B45309' }}>${(exp.amount || 0).toFixed(2)} USD</span>
                      <span className={`status-badge ${exp.status}`}>{exp.status}</span>
                    </div>
                    <p style={{ fontSize: '0.84rem', color: 'var(--text-secondary)' }}>{exp.justification}</p>
                  </div>
                ))
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="modal-footer">
          <button className="btn-secondary" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}
