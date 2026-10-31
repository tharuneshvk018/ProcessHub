import React, { useState } from 'react';
import { 
  Shield, 
  Bell, 
  PlusCircle, 
  UserCheck, 
  ChevronDown,
  Check, 
  ExternalLink,
  Layers,
  User as UserIcon
} from 'lucide-react';

const ROLES = [
  { key: 'ROLE_REPORTER', label: 'Reporter: Priya Sharma', roleName: 'Reporter', persona: 'Priya Sharma', department: 'Facilities' },
  { key: 'ROLE_SUPERVISOR', label: 'Supervisor: Arjun Kumar', roleName: 'Supervisor', persona: 'Arjun Kumar', department: 'Facilities & Maintenance' },
  { key: 'ROLE_TECHNICIAN', label: 'Technician: Karthik Raj', roleName: 'Technician', persona: 'Karthik Raj', department: 'Facilities & Maintenance' },
  { key: 'ROLE_FINANCE_APPROVER', label: 'Finance: Divya Nair', roleName: 'Finance Approver', persona: 'Divya Nair', department: 'Finance Administration' },
  { key: 'ROLE_ADMIN', label: 'Administrator: Rahul Verma', roleName: 'Administrator', persona: 'Rahul Verma', department: 'HQ Operations & Admin' },
];

const NAVIGATION_TABS = [
  { key: 'reporter', label: 'My Reports' },
  { key: 'supervisor', label: 'Supervisor Queue' },
  { key: 'technician', label: 'Technician Workboard' },
  { key: 'finance', label: 'Finance Queue' },
  { key: 'verification', label: 'Verification' },
  { key: 'analytics', label: 'Analytics & SLA' },
];

export default function Navbar({ 
  currentRole = 'ROLE_REPORTER',
  currentUser,
  onSelectRole,
  notifications = [], 
  onMarkNotificationRead, 
  onMarkAllRead, 
  onSelectNotification,
  onOpenReportModal,
  activeTab,
  setActiveTab
}) {
  const [showNotifs, setShowNotifs] = useState(false);
  const [showRoleMenu, setShowRoleMenu] = useState(false);

  // Recipient-specific notification filtering per active perspective
  const filteredNotifications = (notifications || []).filter(n => {
    if (currentRole === 'ROLE_ADMIN') {
      return n.recipientRole === 'ROLE_ADMIN' || n.type === 'SLA_BREACH_WARNING' || n.type === 'SYSTEM_ALERT';
    }
    return n.recipientRole === currentRole || (Array.isArray(n.recipientRoles) && n.recipientRoles.includes(currentRole));
  });

  const unreadCount = filteredNotifications.filter(n => !n.read).length;
  const currentRoleConfig = ROLES.find(r => r.key === currentRole) || ROLES[0];

  return (
    <header className="app-header">
      {/* Brand & Identity */}
      <div className="brand-section">
        <div className="brand-logo-badge">
          <Shield size={22} color="#FFFFFF" />
        </div>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span className="brand-title">ProcessHub</span>
            <span className="brand-tag">Enterprise</span>
          </div>
          <p style={{ fontSize: '0.72rem', color: 'var(--text-muted)', lineHeight: 1 }}>
            Intelligent Workflow & Resolution Platform
          </p>
        </div>
      </div>

      {/* Main Navigation Tabs */}
      <nav className="nav-tabs">
        {NAVIGATION_TABS.map(tab => (
          <button 
            key={tab.key}
            className={`nav-tab-btn ${activeTab === tab.key ? 'active' : ''}`}
            onClick={() => setActiveTab(tab.key)}
            id={`nav-tab-${tab.key}`}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      {/* Header Actions */}
      <div className="header-actions">
        {/* Primary Report CTA */}
        <button 
          className="btn-primary" 
          onClick={onOpenReportModal}
          id="btn-report-issue"
          title="Submit new problem report"
        >
          <PlusCircle size={17} />
          <span>Report Problem</span>
        </button>

        {/* Recipient-Specific Notification Bell */}
        <div style={{ position: 'relative' }}>
          <button 
            style={{ 
              position: 'relative', 
              background: 'var(--bg-primary)', 
              border: '1px solid var(--border-subtle)', 
              borderRadius: 'var(--radius-md)', 
              padding: '8px 10px',
              color: 'var(--text-secondary)',
              cursor: 'pointer'
            }}
            onClick={() => setShowNotifs(!showNotifs)}
            id="btn-notification-bell"
            title={`Notifications for ${currentRoleConfig.persona}`}
          >
            <Bell size={18} />
            {unreadCount > 0 && (
              <span style={{
                position: 'absolute',
                top: '-4px',
                right: '-4px',
                background: 'var(--rose-500)',
                color: '#FFF',
                borderRadius: 'var(--radius-full)',
                fontSize: '0.68rem',
                fontWeight: 700,
                width: '18px',
                height: '18px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: '0 0 6px var(--rose-glow)'
              }}>
                {unreadCount}
              </span>
            )}
          </button>

          {/* Notifications Dropdown Drawer */}
          {showNotifs && (
            <div style={{
              position: 'absolute',
              right: 0,
              top: '44px',
              width: '380px',
              background: 'var(--bg-primary)',
              border: '1px solid var(--border-medium)',
              borderRadius: 'var(--radius-lg)',
              boxShadow: 'var(--shadow-lg)',
              zIndex: 60,
              overflow: 'hidden'
            }}>
              <div style={{
                padding: '12px 16px',
                borderBottom: '1px solid var(--border-subtle)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                background: 'var(--bg-secondary)'
              }}>
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.88rem', color: 'var(--text-primary)' }}>
                    {currentRoleConfig.roleName} Notifications
                  </div>
                  <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                    Delivered to {currentUser?.fullName || currentRoleConfig.persona} ({currentUser?.email || 'authenticated'})
                  </div>
                </div>
                {unreadCount > 0 && (
                  <button 
                    style={{ 
                      background: 'transparent', 
                      color: 'var(--indigo-400)', 
                      fontSize: '0.78rem', 
                      fontWeight: 600, 
                      cursor: 'pointer', 
                      border: 'none' 
                    }}
                    onClick={() => {
                      if (onMarkAllRead) {
                        onMarkAllRead(filteredNotifications.map(n => n.id));
                      }
                    }}
                  >
                    Mark all read
                  </button>
                )}
              </div>

              <div style={{ maxHeight: '340px', overflowY: 'auto' }}>
                {filteredNotifications.length === 0 ? (
                  <div style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    No recipient notifications for {currentRoleConfig.roleName}.
                  </div>
                ) : (
                  filteredNotifications.map(notif => (
                    <div 
                      key={notif.id}
                      onClick={() => {
                        if (onMarkNotificationRead) onMarkNotificationRead(notif.id);
                        if (onSelectNotification) onSelectNotification(notif);
                        setShowNotifs(false);
                      }}
                      style={{
                        padding: '12px 16px',
                        borderBottom: '1px solid var(--border-subtle)',
                        background: notif.read ? 'transparent' : 'rgba(79, 70, 229, 0.08)',
                        cursor: 'pointer',
                        transition: 'background 0.15s ease'
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                        <span style={{ fontWeight: 600, fontSize: '0.84rem', color: notif.read ? 'var(--text-secondary)' : 'var(--text-primary)' }}>
                          {notif.title}
                        </span>
                        {notif.referenceCode && (
                          <span style={{ fontSize: '0.7rem', color: 'var(--indigo-400)', background: 'rgba(99, 102, 241, 0.12)', padding: '2px 6px', borderRadius: '4px' }}>
                            {notif.referenceCode}
                          </span>
                        )}
                      </div>
                      <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', lineHeight: 1.4, margin: 0 }}>
                        {notif.message}
                      </p>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        {/* User Perspective Selector */}
        <div style={{ position: 'relative' }}>
          <button 
            className="role-switcher"
            onClick={() => setShowRoleMenu(!showRoleMenu)}
            id="btn-role-switcher"
            title="Switch User Perspective"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '6px 12px',
              background: 'var(--bg-primary)',
              border: '1px solid var(--border-medium)',
              borderRadius: 'var(--radius-lg)',
              cursor: 'pointer',
              color: 'var(--text-primary)'
            }}
          >
            <UserCheck size={16} color="var(--indigo-500)" />
            <span className={`role-tag-pill ${currentRole}`} style={{ fontSize: '0.8rem', fontWeight: 600 }}>
              {currentRoleConfig.label}
            </span>
            <ChevronDown size={14} color="var(--text-muted)" />
          </button>

          {showRoleMenu && (
            <div style={{
              position: 'absolute',
              right: 0,
              top: '44px',
              width: '280px',
              background: 'var(--bg-primary)',
              border: '1px solid var(--border-medium)',
              borderRadius: 'var(--radius-lg)',
              boxShadow: 'var(--shadow-lg)',
              zIndex: 70,
              padding: '8px',
            }}>
              <div style={{ 
                padding: '6px 10px', 
                fontSize: '0.72rem', 
                color: 'var(--text-muted)', 
                fontWeight: 600, 
                textTransform: 'uppercase',
                letterSpacing: '0.05em'
              }}>
                Select User Perspective:
              </div>
              {ROLES.map(r => (
                <button
                  key={r.key}
                  onClick={() => {
                    onSelectRole && onSelectRole(r.key);
                    setShowRoleMenu(false);
                  }}
                  style={{
                    width: '100%',
                    textAlign: 'left',
                    padding: '8px 12px',
                    borderRadius: 'var(--radius-md)',
                    background: currentRole === r.key ? 'rgba(99, 102, 241, 0.12)' : 'transparent',
                    color: currentRole === r.key ? 'var(--text-primary)' : 'var(--text-secondary)',
                    fontSize: '0.84rem',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    cursor: 'pointer',
                    border: 'none',
                    marginBottom: '2px',
                    transition: 'background 0.15s ease'
                  }}
                >
                  <div>
                    <div style={{ fontWeight: currentRole === r.key ? 700 : 500, color: currentRole === r.key ? 'var(--indigo-400)' : 'var(--text-primary)' }}>
                      {r.label}
                    </div>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                      {r.department}
                    </div>
                  </div>
                  {currentRole === r.key && <Check size={16} color="var(--indigo-400)" />}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
