import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable()
export class AuthService {
    private loginUrl = '';
    private logoutUrl = '';

    redirectUrl: string;
    isLoggedIn = false;

    constructor(
        private http: HttpClient,
    ) {}

    login(value: any): void {}
    logout(): void {}
    onAuthError(): void {}
}
