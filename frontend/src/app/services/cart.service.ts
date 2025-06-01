import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, Observable, of, tap, throwError } from 'rxjs';
import { AddItemToCartRequest, CartResponse, UpdatedCartItemQuantityRequest } from '../models/cart.model';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from './auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private apiUrl = 'http://localhost:8080/api/cart';

  private cartSubject = new BehaviorSubject<CartResponse | null>(null);
  public cart$ = this.cartSubject.asObservable();

  constructor(private http: HttpClient, private authService: AuthService) {
    if (this.authService.isLoggedIn()) {
      this.loadCart().subscribe({
        error: (err) => console.error('Failed to load cart initially', err)
      });
    }

    this.authService.isAuthenticated$.subscribe(isAuth => {
      if (isAuth) {
        this.loadCart().subscribe({
          error: (err) => console.error('Failed to load cart on auth change', err)
        });
      } else {
        this.cartSubject.next(null);
      }
    });
  }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    if (token) {
      return new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      });
    }

    console.warn('No auth token found for cart operation. This might lead to an error.');
    return new HttpHeaders({ 'Content-Type': 'application/json' });
  }

  private loadCart(): Observable<CartResponse> {
    return this.http.get<CartResponse>(this.apiUrl, { headers: this.getAuthHeaders() })
      .pipe(
        tap(cart => this.cartSubject.next(cart)),
        catchError(err => {
          console.error('Error loading cart from backend:', err);
          this.cartSubject.next(null);
          return throwError(() => new Error('Failed to load cart'));
        })
      );
  }

  getCart(): Observable<CartResponse> {
    const currentCart = this.cartSubject.value;
    if (currentCart) {
      return of(currentCart);
    } else if (this.authService.isLoggedIn()) {
      return this.loadCart();
    } else {
      return throwError(() => new Error('User not authenticated or cart unavailable.'));
    }
  }

  getOptionalCart(): Observable<CartResponse | null> {
    return this.cart$;
  }

  addItemToCart(productId: number, quantity: number): Observable<CartResponse> {
    const payload: AddItemToCartRequest = { productId, quantity };
    return this.http.post<CartResponse>(`${this.apiUrl}/items`, payload, { headers: this.getAuthHeaders() })
      .pipe(
        tap(cart => this.cartSubject.next(cart))
      );
  }

  updateItemQuantity(cartItemId: number, quantity: number): Observable<CartResponse> {
    const payload: UpdatedCartItemQuantityRequest = { quantity };
    return this.http.put<CartResponse>(`${this.apiUrl}/items/${cartItemId}`, payload, { headers: this.getAuthHeaders() })
      .pipe(
        tap(cart => this.cartSubject.next(cart))
      );
  }

  removeItemsFromCart(cartItemId: number): Observable<CartResponse> {
    return this.http.delete<CartResponse>(`${this.apiUrl}/items/${cartItemId}`, { headers: this.getAuthHeaders() })
      .pipe(
        tap(cart => this.cartSubject.next(cart))
      );
  }

  clearCart(): Observable<CartResponse> {
    return this.http.delete<CartResponse>(this.apiUrl, { headers: this.getAuthHeaders() })
      .pipe(
        tap(cart => this.cartSubject.next(cart))
      );
  }

  getCurrentCartValue(): CartResponse | null {
    return this.cartSubject.value;
  }
}
