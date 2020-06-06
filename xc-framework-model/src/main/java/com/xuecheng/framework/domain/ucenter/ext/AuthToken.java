package com.xuecheng.framework.domain.ucenter.ext;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@ToString
@NoArgsConstructor
public class AuthToken {
    /**
     * {
     *     "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU4Nzg0MzIxOCwianRpIjoiZmU4NTdlOWQtZjU4OS00YzA2LWEzZWYtZmZjNjRlY2VjZDI3IiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.CcsNediJdFBqE1j67HhMNan7XM8f8DBy5IhXDcHqMBhkuyLNryuwpft_EQfcless-FGXQGdPFBe3WB2NDCsOE6ArS1-KGQc0cLQLGqypJMefqhSYA3nlCiw4fhC_BCzgRkNpKu5PzzjhJe-b0Am4jVFD7QLp4gFGO_G1cTTtZYhv4-0NzCWKFzm5piUSpkSYBxHeorR6x7O-aovhEzQotFuvtJ33ymDjQOo2M7DsE778U-01l_dMqfhqCnh0RwuG3X1aF11ByqIDPeujbtbt9MZuiwbNo0UDnvXAzDC9m-R7XpTMvXv5C7uPYijW0NRocLoVZsrbpEmJeEzFPCtEsw",
     *     "token_type": "bearer",
     *     "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6Iml0Y2FzdCIsInNjb3BlIjpbImFwcCJdLCJhdGkiOiJmZTg1N2U5ZC1mNTg5LTRjMDYtYTNlZi1mZmM2NGVjZWNkMjciLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU4Nzg0MzIxOCwianRpIjoiNWY1ZjhlOTgtM2ViOC00NDI3LTgwOTYtYzJhZTQ1YWRmMmU0IiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.RcSjdGxjcQ3u299aI-DlgY-TKAmXBpmW4kVru9-SO8VWpGXZ4dTX93L88x1BhFueVFyN_KO55ZlGiMV07bO64wtI-QcsfgGnD-wsvAxpgcIAzdfM-17zBivT_OpnWupLi4q1MPfL5eoGUmtqEdZsHOYnMbwlINilFUfS2Co-3ga2oZL44auk-H6deqduLOi3OynODAA8YvkJUoj1x5m0nF9oBBO2hN8jnMZlMWQeSwBplRqtnsXrBmwEEBBkHE84o_WJtqKKMkaCuC8BIlsBx3WzqbftjAjUIw2tT3xbgydrsYlcPZWr_W96pxfl-5hJKcTTAeqD55z3AFeSmswZIA",
     *     "expires_in": 43199,
     *     "scope": "app",
     *     "jti": "fe857e9d-f589-4c06-a3ef-ffc64ececd27"
     * }
     */
    String access_token;//访问token jti用户身份令牌 也就是上面的"jti"
    String refresh_token;//刷新token 也就是上面的"refresh_token"
    String jwt_token;//jwt令牌  也就是上面的"access_token"
}
