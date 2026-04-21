/**
 * Numix Global UI
 * Shared JavaScript for all pages using layout/base.html
 */

'use strict';

var THEME_STORAGE_KEY = 'numix-theme';

function getUiLanguage() {
  var lang = (document.documentElement.getAttribute('lang') || 'es').toLowerCase();
  return lang.indexOf('en') === 0 ? 'en' : 'es';
}

function getStoredTheme() {
  try {
    return localStorage.getItem(THEME_STORAGE_KEY);
  } catch (e) {
    return null;
  }
}

function getPreferredTheme() {
  var stored = getStoredTheme();
  if (stored === 'light' || stored === 'dark') return stored;
  return window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
}

function applyTheme(theme, persist) {
  var resolved = theme === 'light' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', resolved);

  if (persist) {
    try {
      localStorage.setItem(THEME_STORAGE_KEY, resolved);
    } catch (e) {
      // Ignore storage errors in private mode/restricted environments.
    }
  }

  var toggleButton = document.getElementById('themeToggle');
  if (!toggleButton) return;

  var icon = toggleButton.querySelector('i');
  if (icon) {
    icon.className = resolved === 'light' ? 'bi bi-moon-stars' : 'bi bi-sun';
  }

  var isEnglish = getUiLanguage() === 'en';
  var label = resolved === 'light'
    ? (isEnglish ? 'Switch to dark mode' : 'Cambiar a modo oscuro')
    : (isEnglish ? 'Switch to light mode' : 'Cambiar a modo claro');
  toggleButton.setAttribute('title', label);
  toggleButton.setAttribute('aria-label', label);
}

function initThemeToggle() {
  applyTheme(getPreferredTheme(), false);

  var toggleButton = document.getElementById('themeToggle');
  if (toggleButton) {
    toggleButton.addEventListener('click', function () {
      var current = document.documentElement.getAttribute('data-theme') || 'dark';
      var next = current === 'light' ? 'dark' : 'light';
      applyTheme(next, true);
    });
  }

  var media = window.matchMedia('(prefers-color-scheme: light)');
  if (typeof media.addEventListener === 'function') {
    media.addEventListener('change', function () {
      if (!getStoredTheme()) {
        applyTheme(getPreferredTheme(), false);
      }
    });
  } else if (typeof media.addListener === 'function') {
    media.addListener(function () {
      if (!getStoredTheme()) {
        applyTheme(getPreferredTheme(), false);
      }
    });
  }
}

function initTooltips() {
  if (typeof bootstrap === 'undefined') return;
  document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(function (el) {
    new bootstrap.Tooltip(el);
  });
}

function initPopovers() {
  if (typeof bootstrap === 'undefined') return;
  document.querySelectorAll('[data-bs-toggle="popover"]').forEach(function (el) {
    new bootstrap.Popover(el);
  });
}

function initSidebar() {
  var toggle = document.getElementById('sidebarCollapse');
  var sidebar = document.getElementById('sidebar');
  if (!toggle || !sidebar) return;

  var backdrop = document.createElement('div');
  backdrop.className = 'sidebar-backdrop';
  document.body.appendChild(backdrop);

  function isMobile() {
    return window.innerWidth < 992;
  }

  toggle.addEventListener('click', function () {
    if (isMobile()) {
      document.body.classList.toggle('sidebar-mobile-open');
    } else {
      sidebar.classList.toggle('active');
      document.body.classList.toggle('mini-navbar');
    }
  });

  backdrop.addEventListener('click', function () {
    document.body.classList.remove('sidebar-mobile-open');
  });

  window.addEventListener('resize', function () {
    if (!isMobile()) {
      document.body.classList.remove('sidebar-mobile-open');
    }
  });
}

function initSidebarNav() {
  var menu = document.getElementById('menu1');
  if (!menu) return;

  menu.querySelectorAll('.has-arrow').forEach(function (link) {
    link.addEventListener('click', function (e) {
      e.preventDefault();
      var parent = this.parentElement;
      var submenu = parent.querySelector('.submenu-angle');
      if (!submenu) return;

      Array.prototype.forEach.call(parent.parentElement.children, function (sibling) {
        if (sibling === parent) return;
        sibling.classList.remove('active');
        var siblingSubmenu = sibling.querySelector('.submenu-angle');
        if (siblingSubmenu) siblingSubmenu.style.display = 'none';
      });

      parent.classList.toggle('active');
      submenu.style.display = submenu.style.display === 'block' ? 'none' : 'block';
    });
  });

  var currentPath = window.location.pathname;
  menu.querySelectorAll('a[href]').forEach(function (link) {
    var href = link.getAttribute('href');
    if (!href || href === '#' || href.startsWith('javascript:')) return;

    var normalizedHref;
    try {
      normalizedHref = new URL(href, window.location.origin).pathname;
    } catch (e) {
      return;
    }

    if (normalizedHref !== currentPath) return;

    var li = link.closest('li');
    if (li) li.classList.add('active');

    var parentSubmenu = link.closest('.submenu-angle');
    if (parentSubmenu) {
      parentSubmenu.style.display = 'block';
      var parentLi = parentSubmenu.parentElement;
      if (parentLi) parentLi.classList.add('active');
    }
  });
}

function initStickyHeader() {
  var stickyEl = document.querySelector('.sicker-menu');
  if (!stickyEl) return;

  var offsetTop = stickyEl.offsetTop;

  function handleScroll() {
    if (window.scrollY >= offsetTop) {
      stickyEl.classList.add('is-sticky');
      stickyEl.style.position = 'fixed';
      stickyEl.style.top = '0';
      stickyEl.style.width = '100%';
      stickyEl.style.zIndex = '9999';
    } else {
      stickyEl.classList.remove('is-sticky');
      stickyEl.style.position = '';
      stickyEl.style.top = '';
      stickyEl.style.width = '';
      stickyEl.style.zIndex = '';
    }
  }

  window.addEventListener('scroll', handleScroll, { passive: true });
  handleScroll();
}

function initScrollToTop() {
  var existingBtn = document.getElementById('scrollUp');
  if (existingBtn) return;

  var btn = document.createElement('a');
  btn.id = 'scrollUp';
  btn.href = '#';
  btn.innerHTML = '<i class="bi bi-chevron-up"></i>';
  btn.style.cssText = [
    'display:none',
    'position:fixed',
    'bottom:20px',
    'right:20px',
    'z-index:9999',
    'width:40px',
    'height:40px',
    'line-height:40px',
    'text-align:center',
    'border-radius:4px',
    'background:#ec4445',
    'color:#fff',
    'font-size:18px',
    'text-decoration:none',
    'transition:opacity 0.3s'
  ].join(';');

  document.body.appendChild(btn);

  window.addEventListener('scroll', function () {
    if (window.scrollY > 300) {
      btn.style.display = 'block';
      btn.style.opacity = '1';
    } else {
      btn.style.opacity = '0';
      setTimeout(function () {
        if (window.scrollY <= 300) btn.style.display = 'none';
      }, 300);
    }
  }, { passive: true });

  btn.addEventListener('click', function (e) {
    e.preventDefault();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  });
}

function initCounters() {
  if (typeof IntersectionObserver === 'undefined') return;

  var counters = document.querySelectorAll('.counter');
  if (!counters.length) return;

  function animateCounter(el) {
    var target = parseInt(el.textContent.replace(/,/g, ''), 10);
    if (isNaN(target)) return;

    var duration = 1500;
    var startTime = null;

    function step(timestamp) {
      if (!startTime) startTime = timestamp;
      var progress = Math.min((timestamp - startTime) / duration, 1);
      var current = Math.floor(progress * target);
      el.textContent = current.toLocaleString();
      if (progress < 1) {
        requestAnimationFrame(step);
      } else {
        el.textContent = target.toLocaleString();
      }
    }

    requestAnimationFrame(step);
  }

  var observer = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
      if (entry.isIntersecting) {
        animateCounter(entry.target);
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.5 });

  counters.forEach(function (counter) {
    observer.observe(counter);
  });
}

document.addEventListener('DOMContentLoaded', function () {
  initThemeToggle();
  initTooltips();
  initPopovers();
  initSidebar();
  initSidebarNav();
  initStickyHeader();
  initScrollToTop();
  initCounters();
});
