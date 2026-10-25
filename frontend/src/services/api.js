// ProcessHub Central API Service

const API_BASE = '/api/v1';

class ApiService {
  constructor() {
    this.token = localStorage.getItem('ph_token') || null;
    this.currentUser = JSON.parse(localStorage.getItem('ph_user') || 'null');
    this.currentOrgId = localStorage.getItem('ph_org_id') || '00000000-0000-0000-0000-000000000001';
  }

  setToken(token) {
    this.token = token;
    if (token) {
      localStorage.setItem('ph_token', token);
    } else {
      localStorage.removeItem('ph_token');
    }
  }

  setCurrentUser(user) {
    this.currentUser = user;
    if (user) {
      localStorage.setItem('ph_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('ph_user');
    }
  }

  setOrgId(orgId) {
    this.currentOrgId = orgId;
    localStorage.setItem('ph_org_id', orgId);
  }

  async request(endpoint, options = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    try {
      const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers,
      });

      if (!response.ok) {
        let errorData = {};
        try {
          errorData = await response.json();
        } catch (e) {
          errorData = { message: `Request failed with status ${response.status}` };
        }
        throw new Error(errorData.message || `Error ${response.status}`);
      }

      // Handle 204 No Content
      if (response.status === 204) return null;

      return await response.json();
    } catch (err) {
      console.warn(`[ProcessHub API] ${options.method || 'GET'} ${endpoint} failed:`, err.message);
      throw err;
    }
  }

  // Auth Endpoints
  async login(email, password) {
    const res = await this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    if (res.token) {
      this.setToken(res.token);
      this.setCurrentUser(res.user);
    }
    return res;
  }

  async register(data) {
    const res = await this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    if (res.token) {
      this.setToken(res.token);
      this.setCurrentUser(res.user);
    }
    return res;
  }

  async getMe() {
    return this.request('/auth/me');
  }

  // Organizations & Configuration
  async getOrganizations() {
    return this.request('/organizations');
  }

  async getDepartments(orgId = this.currentOrgId) {
    return this.request(`/organizations/${orgId}/departments`);
  }

  async getCategories(orgId = this.currentOrgId, departmentId) {
    const query = departmentId ? `?departmentId=${departmentId}` : '';
    return this.request(`/organizations/${orgId}/categories${query}`);
  }

  // Issues & Reporting
  async createIssue(orgId = this.currentOrgId, issueData) {
    return this.request(`/organizations/${orgId}/issues`, {
      method: 'POST',
      body: JSON.stringify(issueData),
    });
  }

  async getMyReports(orgId = this.currentOrgId) {
    return this.request(`/organizations/${orgId}/issues/my-reports`);
  }

  async getReporterDashboard(orgId = this.currentOrgId) {
    return this.request(`/organizations/${orgId}/issues/reporter-dashboard`);
  }

  async getIssue(orgId = this.currentOrgId, issueId) {
    return this.request(`/organizations/${orgId}/issues/${issueId}`);
  }

  async getDirectIssue(referenceCode) {
    return this.request(`/direct/issues/${referenceCode}`);
  }

  async getRecurrence(orgId = this.currentOrgId, issueId) {
    return this.request(`/organizations/${orgId}/issues/${issueId}/recurrence`);
  }

  // Supervisor Workflow
  async getSupervisorQueue(orgId = this.currentOrgId, filters = {}) {
    const params = new URLSearchParams();
    if (filters.status) params.append('status', filters.status);
    if (filters.priority) params.append('priority', filters.priority);
    if (filters.departmentId) params.append('departmentId', filters.departmentId);
    const qs = params.toString() ? `?${params.toString()}` : '';
    return this.request(`/organizations/${orgId}/supervisor/queue${qs}`);
  }

  async getSupervisorMetrics(orgId = this.currentOrgId) {
    return this.request(`/organizations/${orgId}/supervisor/metrics`);
  }

  async getDepartmentTechnicians(orgId = this.currentOrgId, departmentId) {
    return this.request(`/organizations/${orgId}/supervisor/departments/${departmentId}/technicians`);
  }

  async routeIssue(orgId = this.currentOrgId, issueId) {
    return this.request(`/organizations/${orgId}/issues/${issueId}/route`, {
      method: 'POST',
    });
  }

  async startReview(orgId = this.currentOrgId, issueId) {
    return this.request(`/organizations/${orgId}/issues/${issueId}/review`, {
      method: 'POST',
    });
  }

  async assignTechnician(orgId = this.currentOrgId, issueId, technicianId, instructions) {
    return this.request(`/organizations/${orgId}/issues/${issueId}/assign`, {
      method: 'POST',
      body: JSON.stringify({ technicianId, instructions }),
    });
  }

  async rejectIssue(orgId = this.currentOrgId, issueId, reason) {
    return this.request(`/organizations/${orgId}/issues/${issueId}/reject`, {
      method: 'POST',
      body: JSON.stringify({ reason }),
    });
  }

  async requestInfo(orgId = this.currentOrgId, issueId, question) {
    return this.request(`/organizations/${orgId}/issues/${issueId}/request-info`, {
      method: 'POST',
      body: JSON.stringify({ question }),
    });
  }

  async provideInfo(orgId = this.currentOrgId, issueId, response) {
    return this.request(`/organizations/${orgId}/issues/${issueId}/provide-info`, {
      method: 'POST',
      body: JSON.stringify({ response }),
    });
  }

  // Technician Workflow
  async getTechnicianWorkboard(orgId = this.currentOrgId, filters = {}) {
    const params = new URLSearchParams();
    if (filters.status) params.append('status', filters.status);
    const qs = params.toString() ? `?${params.toString()}` : '';
    return this.request(`/organizations/${orgId}/technician/workboard${qs}`);
  }

  async getTechnicianMetrics(orgId = this.currentOrgId) {
    return this.request(`/organizations/${orgId}/technician/metrics`);
  }

  async startWork(orgId = this.currentOrgId, issueId) {
    return this.request(`/organizations/${orgId}/issues/${issueId}/start`, {
      method: 'POST',
    });
  }

  async updateProgress(orgId = this.currentOrgId, issueId, notes, progressPercentage) {
    return this.request(`/organizations/${orgId}/issues/${issueId}/progress`, {
      method: 'POST',
      body: JSON.stringify({ notes, progressPercentage }),
    });
  }

  async requestExpenditure(orgId = this.currentOrgId, issueId, amount, justification) {
    return this.request(`/organizations/${orgId}/issues/${issueId}/expenditures`, {
      method: 'POST',
      body: JSON.stringify({ amount, justification }),
    });
  }

  async submitResolution(orgId = this.currentOrgId, issueId, data) {
    return this.request(`/organizations/${orgId}/issues/${issueId}/resolve`, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  // Finance Workflow
  async getFinanceQueue(orgId = this.currentOrgId) {
    return this.request(`/organizations/${orgId}/finance/queue`);
  }

  async getFinanceMetrics(orgId = this.currentOrgId) {
    return this.request(`/organizations/${orgId}/finance/metrics`);
  }

  async decideExpenditure(orgId = this.currentOrgId, issueId, expenditureId, approved, notes) {
    return this.request(`/organizations/${orgId}/issues/${issueId}/expenditures/${expenditureId}/decision`, {
      method: 'POST',
      body: JSON.stringify({ approved, notes }),
    });
  }

  // Verification Workflow
  async verifyResolution(orgId = this.currentOrgId, issueId, approved, verificationNotes) {
    return this.request(`/organizations/${orgId}/issues/${issueId}/verify`, {
      method: 'POST',
      body: JSON.stringify({ approved, verificationNotes }),
    });
  }

  // Analytics & SLA Escalation
  async getAnalyticsOverview(orgId = this.currentOrgId) {
    return this.request(`/organizations/${orgId}/analytics/overview`);
  }

  async triggerSlaEscalation(orgId = this.currentOrgId) {
    return this.request(`/organizations/${orgId}/analytics/sla-escalate`, {
      method: 'POST',
    });
  }

  // Notifications
  async getNotifications(orgId = this.currentOrgId, unreadOnly = false) {
    return this.request(`/organizations/${orgId}/notifications?unreadOnly=${unreadOnly}`);
  }

  async getUnreadNotificationCount(orgId = this.currentOrgId) {
    return this.request(`/organizations/${orgId}/notifications/unread-count`);
  }

  async markNotificationRead(orgId = this.currentOrgId, id) {
    return this.request(`/organizations/${orgId}/notifications/${id}/read`, {
      method: 'PATCH',
    });
  }

  async markAllNotificationsRead(orgId = this.currentOrgId) {
    return this.request(`/organizations/${orgId}/notifications/mark-all-read`, {
      method: 'POST',
    });
  }
}

export const api = new ApiService();
export default api;
