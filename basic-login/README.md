# pinpoint-basic-login

Use `-Dpinpoint.modules.web.login=basicLogin` to enable basic login.

The `pinpointJwt` cookie is created with `HttpOnly` enabled by default.
The `Secure` flag is disabled by default and can be enabled when Pinpoint Web is served over HTTPS.
The cookie's `SameSite` attribute defaults to `Lax`.
Use `web.security.auth.jwt.cookie.http-only`, `web.security.auth.jwt.cookie.secure`, and `web.security.auth.jwt.cookie.same-site` to change these flags.

For Docker or Kubernetes environment variables, use `WEB_SECURITY_AUTH_JWT_COOKIE_HTTP_ONLY`,
`WEB_SECURITY_AUTH_JWT_COOKIE_SECURE`, and `WEB_SECURITY_AUTH_JWT_COOKIE_SAME_SITE`.
Set `WEB_SECURITY_AUTH_JWT_COOKIE_SECURE=true` when Pinpoint Web is served over HTTPS.
