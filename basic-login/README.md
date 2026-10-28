# pinpoint-basic-login

Use `-Dpinpoint.modules.web.login=basicLogin` to enable basic login.

The `pinpointJwt` cookie is created with `HttpOnly` enabled by default.
The `Secure` flag is disabled by default and can be enabled when Pinpoint Web is served over HTTPS.
The cookie's `SameSite` attribute defaults to `Lax`.
Use `web.security.auth.jwt.cookie.http-only`, `web.security.auth.jwt.cookie.secure`, and `web.security.auth.jwt.cookie.same-site` to change these flags.

For Docker or Kubernetes environment variables, use `WEB_SECURITY_AUTH_JWT_COOKIE_HTTP_ONLY`,
`WEB_SECURITY_AUTH_JWT_COOKIE_SECURE`, and `WEB_SECURITY_AUTH_JWT_COOKIE_SAME_SITE`.
Set `WEB_SECURITY_AUTH_JWT_COOKIE_SECURE=true` when Pinpoint Web is served over HTTPS.

## Authorization scope

Basic login provides authentication and two roles only:
- `ROLE_USER` can use every API except `/api/admin/**`.
- `ROLE_ADMIN` can also use `/api/admin/**`.

Basic login does not restrict access per application. Every authenticated user can read
and change the data of every application, including alarm rules.

Some controllers carry `@PreAuthorize("@naverPermissionEvaluator...")` annotations.
They are extension points for deployments that provide their own authorization.
Pinpoint does not enable method security and does not ship the evaluator bean,
so these annotations have no effect by default.

To restrict access per application, do all of the following:
1) Enable method security (`@EnableMethodSecurity`).
2) Register a bean named `naverPermissionEvaluator` that implements the methods the annotations call.
3) Register a `ServerMapDataFilter` bean to filter the server map and the real-time active thread count.
