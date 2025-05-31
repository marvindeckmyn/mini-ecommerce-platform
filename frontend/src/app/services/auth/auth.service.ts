import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';

interface LoginRequest {
  username?: string;
  password?: string;
}

interface SignupRequest {
  username?: string;
  email?: string;
  password?: string;
}

interface JwtResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

const AUTH_API_URL = 'http://localhost:8080/api/auth/';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient) { }

  login(credentials: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(AUTH_API_URL + 'signin', credentials, httpOptions)
      .pipe(
        tap(response => {
          this.saveToken(response.token);
          this.saveUser(response);
          this.isAuthenticatedSubject.next(true);
        })
      );
  }

  register(user: SignupRequest): Observable<any> {
    return this.http.post(AUTH_API_URL + 'signup', user, httpOptions);
  }

  logout(): void {
    window.localStorage.removeItem(TOKEN_KEY);
    window.localStorage.removeItem(USER_KEY);
    this.isAuthenticatedSubject.next(false);
  }

  public saveToken(token: string): void {
    window.localStorage.removeItem(TOKEN_KEY);
    window.localStorage.setItem(TOKEN_KEY, token);
  }

  public getToken(): string | null {
    return window.localStorage.getItem(TOKEN_KEY);
  }

  public saveUser(user: JwtResponse): void {
    window.localStorage.removeItem(USER_KEY);
    window.localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  public getUser(): JwtResponse | null {
    const user = window.localStorage.getItem(USER_KEY);
    if (user) {
      return JSON.parse(user);
    }
    return null;
  }

  private hasToken(): boolean {
    return !!this.getToken();
  }

  public isLoggedIn(): boolean {
    const loggedIn = this.hasToken();
    if (this.isAuthenticatedSubject.value !== loggedIn) {
      this.isAuthenticatedSubject.next(loggedIn);
    }
    return loggedIn;
  }

  public getCurrentUsername(): string | null {
    const user = this.getUser();
    return user ? user.username : null;
  }
}
