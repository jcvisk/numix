import { axiosGetJson } from './http.js';

const i18nCache = new Map();

function resolveModuleKey(moduleKey) {
    if (moduleKey && typeof moduleKey === 'string' && moduleKey.trim().length > 0) {
        return moduleKey.trim();
    }

    if (document.body && document.body.dataset && document.body.dataset.module) {
        const value = document.body.dataset.module.trim();
        if (value.length > 0) {
            return value;
        }
    }

    return '';
}

function buildI18nUrl(moduleKey) {
    if (!moduleKey) {
        return '/api/i18n';
    }
    return `/api/i18n/${encodeURIComponent(moduleKey)}`;
}

export async function loadI18n(moduleKey) {
    const resolvedModuleKey = resolveModuleKey(moduleKey);
    const cacheKey = resolvedModuleKey || '';
    if (i18nCache.has(cacheKey)) {
        return i18nCache.get(cacheKey);
    }

    const messages = await axiosGetJson(buildI18nUrl(resolvedModuleKey));
    const safeMessages = messages || {};
    i18nCache.set(cacheKey, safeMessages);
    return safeMessages;
}

export function translate(i18nMap, key, fallback) {
    if (i18nMap && Object.prototype.hasOwnProperty.call(i18nMap, key)) {
        return i18nMap[key];
    }
    return fallback;
}
