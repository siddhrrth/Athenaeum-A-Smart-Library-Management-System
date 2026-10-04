/* ==========================================================================
   Athenaeum — shared application script
   Handles: password visibility, login form validation + simulated auth,
   and small reusable UI helpers used across pages.
   ========================================================================== */

(function () {
  'use strict';

  /**
   * Toggle a password field between hidden and visible text,
   * swapping the icon and updating the accessible label.
   */
  function initPasswordToggle() {
    const toggleBtn = document.getElementById('togglePassword');
    const passwordField = document.getElementById('loginPassword');
    if (!toggleBtn || !passwordField) return;

    toggleBtn.addEventListener('click', function () {
      const isHidden = passwordField.getAttribute('type') === 'password';
      passwordField.setAttribute('type', isHidden ? 'text' : 'password');

      const icon = toggleBtn.querySelector('i');
      icon.classList.toggle('bi-eye', !isHidden);
      icon.classList.toggle('bi-eye-slash', isHidden);
      toggleBtn.setAttribute('aria-label', isHidden ? 'Hide password' : 'Show password');
    });
  }

  /**
   * Lightweight field validation: marks a field invalid/valid and
   * shows/hides its paired error message without a full page framework.
   */
  function setFieldValidity(fieldEl, isValid) {
    const wrapper = fieldEl.closest('.mb-3, .mb-2') || fieldEl.parentElement;
    fieldEl.classList.toggle('is-invalid', !isValid);
    if (wrapper) wrapper.classList.toggle('is-invalid', !isValid);
  }

  function isValidEmailOrId(value) {
    const trimmed = value.trim();
    if (trimmed.length === 0) return false;
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    // Accept either an email address or a member ID of 4+ characters
    return emailPattern.test(trimmed) || trimmed.length >= 4;
  }

  /**
   * Demo credential store for this UI-only build (no backend is connected).
   * In the full DBMS-backed version this check would be a server call.
   */
  var DEMO_ACCOUNT = {
    email: 'amrita@gmail.com',
    password: 'Amrita'
  };

  /**
   * Wires up the login form: validates on submit, checks the demo
   * credentials, shows a brief loading state on the submit button,
   * then either signs in or reports an invalid attempt.
   */
  function initLoginForm() {
    const form = document.getElementById('loginForm');
    if (!form) return;

    const emailField = document.getElementById('loginEmail');
    const passwordField = document.getElementById('loginPassword');
    const submitBtn = document.getElementById('loginSubmit');

    form.addEventListener('submit', function (event) {
      event.preventDefault();

      const emailOk = isValidEmailOrId(emailField.value);
      const passwordOk = passwordField.value.length >= 6;

      setFieldValidity(emailField, emailOk);
      setFieldValidity(passwordField, passwordOk);

      if (!emailOk) {
        emailField.focus();
        return;
      }
      if (!passwordOk) {
        passwordField.focus();
        return;
      }

      // Authenticate with Tomcat backend, or fallback to demo account
      submitBtn.classList.add('is-loading');
      submitBtn.disabled = true;

      const roleSelected = (document.querySelector('input[name="role"]:checked') || {}).value || 'admin';
      const payload = {
        username: emailField.value.trim(),
        email: emailField.value.trim(),
        password: passwordField.value,
        role: roleSelected
      };

      const API_URL = 'http://localhost:8081/backend/api/auth/login';

      fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      .then(function (res) { return res.json(); })
      .then(function (data) {
        submitBtn.classList.remove('is-loading');
        submitBtn.disabled = false;

        if (data && data.success && data.user) {
          if (window.AppUI && window.AppUI.rememberSession) {
            window.AppUI.rememberSession(document.getElementById('rememberMe').checked, data.user);
          }
          showToast('Signed in successfully as ' + data.user.name + '. Redirecting…', 'success');
          window.setTimeout(function () {
            window.location.href = 'dashboard.html';
          }, 600);
        } else {
          setFieldValidity(emailField, false);
          setFieldValidity(passwordField, false);
          showToast(data.message || 'Invalid credentials', 'danger');
          passwordField.value = '';
          passwordField.focus();
        }
      })
      .catch(function () {
        // Fallback to local demo account if Tomcat server is not running
        submitBtn.classList.remove('is-loading');
        submitBtn.disabled = false;

        const emailMatches = emailField.value.trim().toLowerCase() === DEMO_ACCOUNT.email;
        const passwordMatches = passwordField.value === DEMO_ACCOUNT.password;

        if (emailMatches && passwordMatches) {
          if (window.AppUI && window.AppUI.rememberSession) {
            window.AppUI.rememberSession(document.getElementById('rememberMe').checked);
          }
          showToast('Signed in. Redirecting to your dashboard…', 'success');
          window.setTimeout(function () {
            window.location.href = 'dashboard.html';
          }, 600);
        } else {
          setFieldValidity(emailField, false);
          setFieldValidity(passwordField, false);
          showToast('Credentials did not match local demo or database records.', 'danger');
          passwordField.value = '';
          passwordField.focus();
        }
      });
    });

    // Clear the invalid state as soon as the person starts correcting a field.
    [emailField, passwordField].forEach(function (field) {
      field.addEventListener('input', function () {
        if (field.classList.contains('is-invalid')) {
          setFieldValidity(field, true);
        }
      });
    });
  }

  /**
   * "Forgot password" is a placeholder in this UI-only build —
   * acknowledge the click with feedback rather than a dead link.
   */
  function initForgotPassword() {
    const link = document.getElementById('forgotPasswordLink');
    if (!link) return;
    link.addEventListener('click', function (event) {
      event.preventDefault();
      showToast('Password reset instructions would be sent to your email.', 'info');
    });
  }

  /**
   * Minimal toast helper so pages can surface feedback without
   * wiring up Bootstrap's full toast markup each time.
   */
  function showToast(message, variant) {
    variant = variant || 'primary';
    let container = document.getElementById('appToastContainer');
    if (!container) {
      container = document.createElement('div');
      container.id = 'appToastContainer';
      container.className = 'toast-container position-fixed top-0 end-0 p-3';
      container.style.zIndex = '1080';
      document.body.appendChild(container);
    }

    const iconMap = {
      success: 'bi-check-circle-fill',
      danger: 'bi-x-circle-fill',
      info: 'bi-info-circle-fill',
      warning: 'bi-exclamation-triangle-fill',
      primary: 'bi-bell-fill'
    };
    const colorMap = {
      success: 'var(--color-success)',
      danger: 'var(--color-danger)',
      info: 'var(--color-primary)',
      warning: 'var(--color-warning)',
      primary: 'var(--color-primary)'
    };

    const toastEl = document.createElement('div');
    toastEl.className = 'toast align-items-center border-0 shadow-sm mb-2';
    toastEl.setAttribute('role', 'status');
    toastEl.setAttribute('aria-live', 'polite');
    toastEl.style.borderRadius = '12px';
    toastEl.style.overflow = 'hidden';
    toastEl.innerHTML =
      '<div class="d-flex">' +
        '<div class="toast-body d-flex align-items-center gap-2">' +
          '<i class="bi ' + iconMap[variant] + '" style="color:' + colorMap[variant] + '"></i>' +
          '<span style="font-size:14px">' + message + '</span>' +
        '</div>' +
        '<button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>' +
      '</div>';

    container.appendChild(toastEl);
    const toast = new bootstrap.Toast(toastEl, { delay: 3200 });
    toast.show();
    toastEl.addEventListener('hidden.bs.toast', function () {
      toastEl.remove();
    });
  }

  /**
   * Minimal session helper for this front-end-only build. Stores a flag
   * and the signed-in user's display info so other pages (dashboard, etc.)
   * can greet the person and guard against being viewed pre-login.
   */
  function rememberSession(persist, user) {
    const store = persist ? window.localStorage : window.sessionStorage;
    const roleChecked = (document.querySelector('input[name="role"]:checked') || {}).value || 'admin';
    store.setItem('athenaeum_session', JSON.stringify({
      name: (user && user.name) || 'Amrita Nair',
      email: (user && user.email) || DEMO_ACCOUNT.email,
      role: (user && user.role) || roleChecked,
      signedInAt: new Date().toISOString()
    }));
  }

  function getSession() {
    const raw = window.localStorage.getItem('athenaeum_session') || window.sessionStorage.getItem('athenaeum_session');
    try {
      return raw ? JSON.parse(raw) : null;
    } catch (err) {
      return null;
    }
  }

  function clearSession() {
    window.localStorage.removeItem('athenaeum_session');
    window.sessionStorage.removeItem('athenaeum_session');
  }

  document.addEventListener('DOMContentLoaded', function () {
    initPasswordToggle();
    initLoginForm();
    initForgotPassword();
  });

  // Expose shared helpers for reuse by other page scripts.
  window.AppUI = window.AppUI || {};
  window.AppUI.showToast = showToast;
  window.AppUI.rememberSession = rememberSession;
  window.AppUI.getSession = getSession;
  window.AppUI.clearSession = clearSession;
})();
