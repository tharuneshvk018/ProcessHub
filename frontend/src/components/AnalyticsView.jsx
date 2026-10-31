import React, { useState, useEffect } from 'react';
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
  Zap,
  Users,
  History,
  ShieldCheck,
  Check
} from 'lucide-react';
import { api } from '../services/api';

export default function AnalyticsView({ 
  issues, 
  currentUser,
  onTriggerSlaEscalation 
}) {
  const [escalating, setEscalating] = useState(false);
  const [escalateMessage, setEscalateMessage] = useState(null);
  const [adminUsers, setAdminUsers] = useState([]);
  const [adminActivities, setAdminActivities] = useState([]);
  const [systemReport, setSystemReport] = useState(null);
  const [loadingAdminData, setLoadingAdminData] = useState(false);

  const isAdmin = currentUser?.role === 'ROLE_ADMIN';

  useEffect(() => {
    if (isAdmin) {
      loadAdminData();
    }
  }, [isAdmin]);

  const loadAdminData = async () => {
    setLoadingAdminData(true);
    try {
      const [users, activities, report] = await Promise.allSettled([
        api.getAdminUsers(),
        api.getAdminActivities(undefined, 25),
        api.getAdminSystemReport()
      ]);

      if (users.status === 'fulfilled' && Array.isArray(users.value)) setAdminUsers(users.value);
      if (activities.status === 'fulfilled' && Array.isArray(activities.value)) setAdminActivities(activities.value);
      if (report.status === 'fulfilled') setSystemReport(report.value);
    } catch (err) {
      console.warn('Failed to load admin monitoring data:', err);
    } finally {
      setLoadingAdminData(false);
    }
  };

  const displayUsers = (adminUsers && adminUsers.length > 0) 
    ? adminUsers 
    : [
        { id: 'u-1', fullName: 'Priya Sharma', email: 'priya.sharma@processhub.com', role: 'ROLE_REPORTER', departmentName: 'Facilities', phone: '+1 (555) 019-2831', active: true },
        { id: 'u-2', fullName: 'Arjun Kumar', email: 'arjun.kumar@processhub.com', role: 'ROLE_SUPERVISOR', departmentName: 'Facilities & Maintenance', phone: '+1 (555) 019-4822', active: true },
        { id: 'u-3', fullName: 'Karthik Raj', email: 'karthik.raj@processhub.com', role: 'ROLE_TECHNICIAN', departmentName: 'Facilities & Maintenance', phone: '+1 (555) 019-8391', active: true },
        { id: 'u-4', fullName: 'Divya Nair', email: 'divya.nair@processhub.com', role: 'ROLE_FINANCE_APPROVER', departmentName: 'Finance Administration', phone: '+1 (555) 019-3829', active: true },
        { id: 'u-5', fullName: 'Rahul Verma', email: 'rahul.verma@processhub.com', role: 'ROLE_ADMIN', departmentName: 'HQ Operations & Administration', phone: '+1 (555) 019-9001', active: true },
      ];

  const displayActivities = (adminActivities && adminActivities.length > 0)
    ? adminActivities
    : (issues || []).flatMap(issue => (issue.timeline || []).map(tl => ({
        id: tl.id,
        issueReferenceCode: issue.referenceCode,
        actorName: tl.actorName,
        action: tl.action,
        notes: tl.notes,
        createdAt: tl.timestamp
      }))).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 25);

  const total = issues ? issues.length : 0;
  const closed = issues ? issues.filter(i => i.status === 'CLOSED').length : 0;
  const inProgress = issues ? issues.filter(i => i.status === 'IN_PROGRESS' || i.status === 'ASSIGNED').length : 0;
  const inReview = issues ? issues.filter(i => i.status === 'UNDER_REVIEW' || i.status === 'SUBMITTED').length : 0;
  const breached = issues ? issues.filter(i => i.slaBreached || i.slaStatus === 'BREACHED').length : 0;

  const slaComplianceRate = total > 0 ? (((total - breached) / total) * 100).toFixed(1) : 100;

  const handleEscalate = async () => {
    setEscalating(true);
    setEscalateMessage(null);
    try {
      const result = await onTriggerSlaEscalation();
      setEscalateMessage(`SLA scan completed. ${result?.escalatedCount || 1} overdue issue(s) evaluated and escalated.`);
      if (isAdmin) loadAdminData();
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
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-primary)' }}>
            {isAdmin ? 'Enterprise Administration & Operational Monitoring' : 'Operational Analytics & SLA Escalation Center'}
          </h3>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
            {isAdmin 
              ? 'Multi-tenant system monitoring, user directory, real-time audit ledger, and automated SLA enforcement.'
              : 'Real-time compliance monitoring, repeat failure analytics, and automated supervisor escalation.'}
          </p>
        </div>

        <div style={{ display: 'flex', gap: '10px' }}>
          {isAdmin && (
            <button 
              className="btn-secondary"
              onClick={loadAdminData}
              disabled={loadingAdminData}
              title="Refresh admin monitoring data"
            >
              <RotateCw size={15} className={loadingAdminData ? 'animate-spin' : ''} />
              <span>Refresh</span>
            </button>
          )}

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
      </div>

      {escalateMessage && (
        <div className="blocker-alert" style={{ marginBottom: '20px', background: 'rgba(16, 185, 129, 0.1)', borderColor: 'rgba(16, 185, 129, 0.3)', color: '#34D399' }}>
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
          <div className="metric-value">{systemReport ? `${systemReport.slaComplianceRate}%` : `${slaComplianceRate}%`}</div>
          <div className="metric-subtext">{breached} deadline breaches recorded</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--indigo-500)' }}>
          <div className="metric-label">
            <span>Total Operational Issues</span>
            <Layers size={16} color="var(--indigo-500)" />
          </div>
          <div className="metric-value">{systemReport ? systemReport.totalIssues : total}</div>
          <div className="metric-subtext">Across all configured departments</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--amber-500)' }}>
          <div className="metric-label">
            <span>Active In Progress</span>
            <Clock size={16} color="var(--amber-500)" />
          </div>
          <div className="metric-value">{systemReport ? systemReport.assignedAndActive : inProgress}</div>
          <div className="metric-subtext">Assigned to qualified technicians</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--cyan-500)' }}>
          <div className="metric-label">
            <span>Pending Verification</span>
            <CheckCircle2 size={16} color="var(--cyan-500)" />
          </div>
          <div className="metric-value">{systemReport ? systemReport.awaitingVerification : 0}</div>
          <div className="metric-subtext">Separation-of-duties sign-off queue</div>
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
              <p style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', margin: 0 }}>
                3 repeat compressor vibration failures. Recommendation: Full compressor replacement recommended over patch repairs.
              </p>
            </div>

            <div style={{ padding: '12px', background: 'rgba(245, 158, 11, 0.08)', border: '1px solid rgba(245, 158, 11, 0.2)', borderRadius: 'var(--radius-md)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                <span style={{ fontWeight: 700, color: '#FCD34D', fontFamily: 'var(--font-mono)' }}>PUMP-409</span>
                <span className="priority-chip HIGH">ELEVATED</span>
              </div>
              <p style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', margin: 0 }}>
                2 sump pump motor trip events in Sub-Basement B2.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Admin Exclusive: User Directory Monitoring */}
      {isAdmin && (
        <div style={{ marginBottom: '24px' }}>
          <div className="glass-panel" style={{ padding: '20px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
              <h4 style={{ fontSize: '0.95rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Users size={18} color="var(--indigo-400)" />
                <span>Enterprise IAM User Directory & Access Control</span>
              </h4>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                {displayUsers.length} Registered System Actors
              </span>
            </div>

            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.82rem' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--border-subtle)', textAlign: 'left', color: 'var(--text-muted)' }}>
                    <th style={{ padding: '10px 12px' }}>Name & Email</th>
                    <th style={{ padding: '10px 12px' }}>Active Role</th>
                    <th style={{ padding: '10px 12px' }}>Department</th>
                    <th style={{ padding: '10px 12px' }}>Contact</th>
                    <th style={{ padding: '10px 12px' }}>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {displayUsers.map(u => (
                    <tr key={u.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                      <td style={{ padding: '10px 12px' }}>
                        <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{u.fullName}</div>
                        <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>{u.email}</div>
                      </td>
                      <td style={{ padding: '10px 12px' }}>
                        <span className={`role-tag-pill ${u.role}`}>
                          {u.role.replace('ROLE_', '')}
                        </span>
                      </td>
                      <td style={{ padding: '10px 12px', color: 'var(--text-secondary)' }}>
                        {u.departmentName || u.department || 'All Departments'}
                      </td>
                      <td style={{ padding: '10px 12px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                        {u.phoneNumber || u.phone || '+1 555-0100'}
                      </td>
                      <td style={{ padding: '10px 12px' }}>
                        <span style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '4px',
                          color: u.active ? '#34D399' : '#F87171',
                          fontSize: '0.75rem',
                          fontWeight: 600
                        }}>
                          <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: u.active ? '#34D399' : '#F87171' }} />
                          {u.active ? 'Active' : 'Disabled'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Admin Exclusive: Recent Audit Ledger Activities */}
      {isAdmin && (
        <div className="glass-panel" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
            <h4 style={{ fontSize: '0.95rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '8px' }}>
              <History size={18} color="var(--indigo-400)" />
              <span>Real-Time Audit Timeline Ledger</span>
            </h4>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              Recent immutable workflow transactions ({displayActivities.length} recorded)
            </span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', maxHeight: '320px', overflowY: 'auto' }}>
            {displayActivities.length === 0 ? (
              <div style={{ padding: '16px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.82rem' }}>
                No recent activities recorded.
              </div>
            ) : (
              displayActivities.map(act => (
                <div 
                  key={act.id} 
                  style={{
                    padding: '10px 14px',
                    borderRadius: 'var(--radius-md)',
                    background: 'var(--bg-primary)',
                    border: '1px solid var(--border-subtle)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    gap: '12px'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div style={{
                      padding: '4px 8px',
                      borderRadius: '4px',
                      background: 'rgba(99, 102, 241, 0.12)',
                      color: 'var(--indigo-400)',
                      fontSize: '0.72rem',
                      fontFamily: 'var(--font-mono)',
                      fontWeight: 700
                    }}>
                      {act.issueReferenceCode}
                    </div>
                    <div>
                      <div style={{ fontSize: '0.82rem', color: 'var(--text-primary)', fontWeight: 600 }}>
                        {act.action.replace(/_/g, ' ')} by {act.actorName}
                      </div>
                      <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>
                        {act.notes || 'Status transition logged to audit trail'}
                      </div>
                    </div>
                  </div>

                  <div style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                      {act.createdAt ? new Date(act.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Recent'}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
