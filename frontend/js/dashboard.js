/* ==========================================================================
   Athenaeum — dashboard page script
   Handles: session guard, sidebar collapse/mobile drawer, dark mode,
   profile menu, logout, and chart initialization.
   ========================================================================== */

(function () {
  'use strict';

  /**
   * This is a UI-only build with no backend, so "auth" is a lightweight
   * session flag set at login. If someone lands here without one, send
   * them back to sign in rather than showing a blank/broken dashboard.
   */
  function guardSession() {
    const session = window.AppUI && window.AppUI.getSession ? window.AppUI.getSession() : null;
    if (!session) {
      window.location.href = 'login.html';
      return null;
    }
    return session;
  }

  function applySessionToUI(session) {
    const initials = session.name
      .split(' ')
      .map(function (part) { return part[0]; })
      .join('')
      .slice(0, 2)
      .toUpperCase();

    document.querySelectorAll('[data-user-name]').forEach(function (el) {
      el.textContent = session.name;
    });
    document.querySelectorAll('[data-user-role]').forEach(function (el) {
      el.textContent = session.role.charAt(0).toUpperCase() + session.role.slice(1);
    });
    document.querySelectorAll('[data-user-initials]').forEach(function (el) {
      el.textContent = initials;
    });
  }

  function initSidebarToggle() {
    const shell = document.getElementById('appShell');
    const collapseBtn = document.getElementById('sidebarCollapseBtn');
    const mobileBtn = document.getElementById('sidebarMobileBtn');
    const overlay = document.getElementById('sidebarOverlay');
    if (!shell) return;

    if (collapseBtn) {
      collapseBtn.addEventListener('click', function () {
        shell.classList.toggle('sidebar-collapsed');
      });
    }
    if (mobileBtn) {
      mobileBtn.addEventListener('click', function () {
        shell.classList.add('sidebar-mobile-open');
      });
    }
    if (overlay) {
      overlay.addEventListener('click', function () {
        shell.classList.remove('sidebar-mobile-open');
      });
    }
  }

  function initDarkModeToggle() {
    const toggle = document.getElementById('darkModeToggle');
    if (!toggle) return;

    const stored = window.localStorage.getItem('athenaeum_theme');
    if (stored === 'dark') {
      document.documentElement.setAttribute('data-theme', 'dark');
      toggle.classList.add('is-dark');
    }

    toggle.addEventListener('click', function () {
      const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
      if (isDark) {
        document.documentElement.removeAttribute('data-theme');
        window.localStorage.setItem('athenaeum_theme', 'light');
        toggle.classList.remove('is-dark');
      } else {
        document.documentElement.setAttribute('data-theme', 'dark');
        window.localStorage.setItem('athenaeum_theme', 'dark');
        toggle.classList.add('is-dark');
      }
    });
  }

  function initLogout() {
    const logoutLinks = document.querySelectorAll('[data-action="logout"]');
    logoutLinks.forEach(function (link) {
      link.addEventListener('click', function (event) {
        event.preventDefault();
        if (window.AppUI && window.AppUI.clearSession) {
          window.AppUI.clearSession();
        }
        window.location.href = 'login.html';
      });
    });
  }

  function initClock() {
    const el = document.getElementById('todayLabel');
    if (!el) return;
    const now = new Date();
    el.textContent = now.toLocaleDateString(undefined, {
      weekday: 'long',
      month: 'long',
      day: 'numeric'
    });
  }

  /**
   * Chart.js palette pulled from the shared design tokens so charts
   * feel native to the rest of the UI rather than using default colors.
   */
  function initCharts() {
    if (typeof Chart === 'undefined') return;

    Chart.defaults.font.family = "'Inter', sans-serif";
    Chart.defaults.color = '#6B7280';

    const borrowCtx = document.getElementById('borrowTrendChart');
    if (borrowCtx) {
      new Chart(borrowCtx, {
        type: 'line',
        data: {
          labels: ['Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul'],
          datasets: [
            {
              label: 'Books issued',
              data: [420, 486, 455, 512, 498, 560],
              borderColor: '#2563EB',
              backgroundColor: 'rgba(37, 99, 235, 0.08)',
              tension: 0.35,
              fill: true,
              pointRadius: 3,
              pointBackgroundColor: '#2563EB'
            },
            {
              label: 'Books returned',
              data: [390, 452, 430, 470, 465, 521],
              borderColor: '#10B981',
              backgroundColor: 'rgba(16, 185, 129, 0.06)',
              tension: 0.35,
              fill: true,
              pointRadius: 3,
              pointBackgroundColor: '#10B981'
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: {
            y: { grid: { color: '#F1F5F9' }, ticks: { stepSize: 150 } },
            x: { grid: { display: false } }
          }
        }
      });
    }

    const categoryCtx = document.getElementById('categoryChart');
    if (categoryCtx) {
      new Chart(categoryCtx, {
        type: 'doughnut',
        data: {
          labels: ['Fiction', 'Science', 'History', 'Technology', 'Biography'],
          datasets: [{
            data: [32, 22, 16, 20, 10],
            backgroundColor: ['#2563EB', '#3B82F6', '#93C5FD', '#10B981', '#F59E0B'],
            borderWidth: 0
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          cutout: '68%',
          plugins: { legend: { display: false } }
        }
      });
    }

    const visitorsCtx = document.getElementById('visitorsChart');
    if (visitorsCtx) {
      new Chart(visitorsCtx, {
        type: 'bar',
        data: {
          labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
          datasets: [{
            label: 'Visitors',
            data: [86, 102, 94, 118, 133, 76, 48],
            backgroundColor: '#3B82F6',
            borderRadius: 5,
            maxBarThickness: 26
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: {
            y: { grid: { color: '#F1F5F9' }, ticks: { stepSize: 40 } },
            x: { grid: { display: false } }
          }
        }
      });
    }
  }

  function fetchLiveStats() {
    const API_URL = 'http://localhost:8081/backend/api/dashboard/stats';
    fetch(API_URL)
      .then(function (res) { return res.json(); })
      .then(function (data) {
        if (data && (data.success || data.stats)) {
          const stats = data.stats || data;
          Object.keys(stats).forEach(function (key) {
            const el = document.querySelector('[data-stat="' + key + '"]');
            if (el) {
              if (key === 'total_fines_collected') {
                el.textContent = '₹' + Number(stats[key]).toLocaleString();
              } else {
                el.textContent = Number(stats[key]).toLocaleString();
              }
            }
          });
        }
      })
      .catch(function () {
        // Fallback to initial display values if Tomcat is not currently running
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    const session = guardSession();
    if (!session) return;

    applySessionToUI(session);
    initSidebarToggle();
    initDarkModeToggle();
    initLogout();
    initClock();
    initCharts();
    fetchLiveStats();
  });
})();
