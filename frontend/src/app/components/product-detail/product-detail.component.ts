import { CommonModule, Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css'
})
export class ProductDetailComponent implements OnInit {
  product: Product | undefined;
  isLoading: boolean = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private location: Location
  ) { }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const productId = +idParam;
      if (isNaN(productId)) {
        this.error = `Invalid Product ID: ${idParam}. ID must be a number.`;
        this.isLoading = false;
        console.error('Invalid Product ID in route parameters:', idParam);
        return;
      }
      this.productService.getProductById(productId).subscribe({
        next: (data) => {
          this.product = data;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error fetching product:', err);
          this.error = `Failed to load product with ID ${productId}. Please ensure the backend is running and the product exists.`;
          if (err.status === 0 || err.message.includes('HttpFailureResponse')) {
            this.error += ' This might be a CORS issue or the backend might be down. Check browser console and backend CORS configuration.';
          }
          this.isLoading = false;
        }
      });
    } else {
      this.error = 'Product ID not provided in the route.';
      this.isLoading = false;
      console.error('Product ID not found in route parameters');
    }
  }

  goBack(): void {
    this.location.back();
  }
}
