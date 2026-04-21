function buildHeaders(extraHeaders) {
    const headers = {
        'X-Requested-With': 'XMLHttpRequest',
        Accept: 'application/json',
        ...(extraHeaders || {})
    };

    const csrfMeta = readCsrfMeta();
    if (csrfMeta.header && csrfMeta.token) {
        headers[csrfMeta.header] = csrfMeta.token;
    }

    return headers;
}

function readCsrfMeta() {
    if (document.body && document.body.dataset && document.body.dataset.csrfHeader && document.body.dataset.csrfToken) {
        return {
            header: document.body.dataset.csrfHeader,
            token: document.body.dataset.csrfToken
        };
    }

    return { header: '', token: '' };
}

function getContextPath() {
    if (!document.body || !document.body.dataset || !document.body.dataset.contextPath) {
        return '';
    }
    return document.body.dataset.contextPath;
}

function normalizeContextPath(contextPath) {
    if (!contextPath || contextPath === '/') {
        return '';
    }
    return contextPath.endsWith('/') ? contextPath.slice(0, -1) : contextPath;
}

function resolveUrl(urlOrPath) {
    if (!urlOrPath) {
        return normalizeContextPath(getContextPath()) || '/';
    }

    if (/^https?:\/\//i.test(urlOrPath) || urlOrPath.startsWith('//')) {
        return urlOrPath;
    }

    const normalizedPath = urlOrPath.startsWith('/') ? urlOrPath : `/${urlOrPath}`;
    return `${normalizeContextPath(getContextPath())}${normalizedPath}`;
}

export function axiosGetJson(urlOrPath) {
    return axios.get(resolveUrl(urlOrPath), {
        headers: buildHeaders()
    }).then((response) => response.data);
}

export function axiosPostJson(urlOrPath, payload) {
    return axios.post(resolveUrl(urlOrPath), payload, {
        headers: buildHeaders({
            'Content-Type': 'application/json'
        })
    }).then((response) => response.data);
}

export function resolveApiErrorMessage(error, fallback) {
    if (error && error.response && error.response.data && error.response.data.message) {
        return error.response.data.message;
    }
    return fallback;
}
