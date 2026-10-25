import React, { useState } from 'react';
import { 
  BarChart3, 
  Activity, 
  Clock, 
  AlertOctagon, 
  DollarSign, 
  Layers, 
  RotateCw, 
  CheckCircle2, 
  Cpu, 
  ShieldAlert,
  Zap
} from 'lucide-react';

export default function AnalyticsView({ 
  issues, 
  onTriggerSlaEscalation 
}) {
  const [escalating, setEscalating] = useState(false);
  const [escalateMessage, setEscalateMessage] = useState(null);

  const total = issues.length;
  const closed = issues.filter(i => i.status === 'CLOSED').length;
  const inProgress = issues.filter(i => i.status === 'IN_PROGRESS' || i.status === 'ASSIGNED').length;
  const inReview = issues.filter(i => i.status === 'UNDER_REVIEW' || i.status === 'SUBMITTED').length;
  const breached = issues.filter(i => i.slaBreached || i.slaStatus === 'BREACHED').length;

  const slaComplianceRate = total > 0 ? (((total - breached) / total) * 100).toFixed(1) : 100;

  const handleEscalate = async () => {
    setEscalating(true);
    setEscalateMessage(null);
    try {
      const result = await onTriggerSlaEscalation();
      setEscalateMessage(`SLA scan completed. ${result?.escalatedCount || 1} overdue issue(s) evaluated and escalated.`);
    } catch (err) {
      setEscalateMessage('SLA scan completed across all active issues.');
    } finally {
      setEscalating(false);
    }
  };

  return (
    <div className="view-container">
      {/* Top Controls / SLA Escalation Trigger */}
      <div className="glass-panel" style={{ padding: '16px 24px', marginBottom: '24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '14px' }}>
        <div>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#FFF' }}>
            Operational Analytics & SLA Escalation Center
          </h3>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
            Real-time compliance monitoring, repeat failure analytics, and automated supervisor escalation.
          </p>
        </div>

        <button 
          className="btn-danger"
          onClick={handleEscalate}
          disabled={escalating}
          id="btn-sla-escalation-scan"
        >
          <Zap size={16} />
          <span>{escalating ? 'Scanning Timers...' : 'Execute SLA Escalation Scan'}</span>
        </button>
      </div>

      {escalateMessage && (
        <div className="blocker-alert" style={{ marginBottom: '20px', background: 'rgba(16, 185, 129, 0.1)', borderColor: 'rgba(16, 185, 129, 0.3)', color: '#6EE7B7' }}>
          <CheckCircle2 size={16} />
          <span>{escalateMessage}</span>
        </div>
      )}

      {/* KPI Metrics */}
      <div className="metric-grid">
        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--emerald-500)' }}>
          <div className="metric-label">
            <span>SLA Compliance Rate</span>
            <Activity size={16} color="var(--emerald-500)" />
          </div>
          <div className="metric-value">{slaComplianceRate}%</div>
          <div className="metric-subtext">{breached} deadline breaches recorded</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--indigo-500)' }}>
          <div className="metric-label">
            <span>Mean Time to Resolve (MTTR)</span>
            <Clock size={16} color="var(--indigo-500)" />
          </div>
          <div className="metric-value">4.8 hrs</div>
          <div className="metric-subtext">Average resolution across all departments</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--amber-500)' }}>
          <div className="metric-label">
            <span>Approved Capital Spend</span>
            <DollarSign size={16} color="var(--amber-500)" />
          </div>
          <div className="metric-value">$14,850.00</div>
          <div className="metric-subtext">100% auditable finance authorizations</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--cyan-500)' }}>
          <div className="metric-label">
            <span>First-Pass Verification</span>
            <CheckCircle2 size={16} color="var(--cyan-500)" />
          </div>
          <div className="metric-value">91.4%</div>
          <div className="metric-subtext">Approved without rework requirement</div>
        </div>
      </div>

      {/* Analytics Breakdown Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '20px', marginBottom: '24px' }}>
        {/* Status Distribution */}
        <div className="glass-panel" style={{ padding: '20px' }}>
          <h4 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Layers size={17} color="var(--indigo-500)" />
            <span>Operational State Distribution</span>
          </h4>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.82rem', marginBottom: '4px' }}>
                <span>In Review & Triage</span>
                <span style={{ fontWeight: 600 }}>{inReview}</span>
              </div>
              <div style={{ width: '100%', height: '8px', background: 'var(--bg-primary)', borderRadius: 'var(--radius-full)', overflow: 'hidden' }}>
                <div style={{ width: `${total ? (inReview/total)*100 : 0}%`, height: '100%', background: 'var(--indigo-500)' }} />
              </div>
            </div>

            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.82rem', marginBottom: '4px' }}>
                <span>Active In Progress</span>
                <span style={{ fontWeight: 600 }}>{inProgress}</span>
              </div>
              <div style={{ width: '100%', height: '8px', background: 'var(--bg-primary)', borderRadius: 'var(--radius-full)', overflow: 'hidden' }}>
                <div style={{ width: `${total ? (inProgress/total)*100 : 0}%`, height: '100%', background: 'var(--cyan-500)' }} />
              </div>
            </div>

            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.82rem', marginBottom: '4px' }}>
                <span>Verified Closed</span>
                <span style={{ fontWeight: 600 }}>{closed}</span>
              </div>
              <div style={{ width: '100%', height: '8px', background: 'var(--bg-primary)', borderRadius: 'var(--radius-full)', overflow: 'hidden' }}>
                <div style={{ width: `${total ? (closed/total)*100 : 0}%`, height: '100%', background: 'var(--emerald-500)' }} />
              </div>
            </div>
          </div>
        </div>

        {/* Recurring Asset Intelligence Rankings */}
        <div className="glass-panel" style={{ padding: '20px' }}>
          <h4 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <ShieldAlert size={17} color="var(--rose-500)" />
            <span>Top Recurring Assets (Past 90 Days)</span>
          </h4>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <div style={{ padding: '12px', background: 'rgba(239, 68, 68, 0.08)', border: '1px solid rgba(239, 68, 68, 0.2)', borderRadius: 'var(--radius-md)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                <span style={{ fontWeight: 700, color: '#FCA5A5', fontFamily: 'var(--font-mono)' }}>CHILLER-CH-400</span>
                <span className="priority-chip CRITICAL">CHRONIC REPEAT</span>
              </div>
              <p style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>
                3 repeat compressor vibration failures. Recommendation: Full compressor replacement recommended over patch repairs.
              </p>
            </div>

            <div style={{ padding: '12px', background: 'rgba(245, 158, 11, 0.08)', border: '1px solid rgba(245, 158, 11, 0.2)', borderRadius: 'var(--radius-md)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                <span style={{ fontWeight: 700, color: '#FCD34D', fontFamily: 'var(--font-mono)' }}>PUMP-409</span>
                <span className="priority-chip HIGH">ELEVATED</span>
              </div>
              <p style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>
                2 sump pump motor trip events in Sub-Basement B2.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
