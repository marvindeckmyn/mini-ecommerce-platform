import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  isLoading: boolean = true;
  error: string | null = null;

  constructor(private productService: ProductService) { }

  ngOnInit(): void {
      this.productService.getProducts().subscribe({
        next: (data) => {
          this.products = data;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error fetching products:', err);
          this.error = 'Failed to load products. Please ensure the backend is running and accessible.';
          if (err.status === 0 || err.message.includes('HttpFailureResponse')) {
            this.error += ' This might be a CORS issue or the backend might be down. Check browser console and backend CORS configuration.';
          }
          this.isLoading = false;
        }
      });
  }
}
