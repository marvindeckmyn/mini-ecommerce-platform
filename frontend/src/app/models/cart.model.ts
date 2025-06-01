export interface CartItemResponse {
    id: number;
    productId: number;
    productName: string;
    productDescription: string;
    productImageUrl: string;
    quantity: number;
    priceWhenAdded: number;
    itemTotalPrice: number;
}

export interface CartResponse {
    id: number;
    userId: number;
    username: string;
    items: CartItemResponse[];
    grandTotal: number;
}

export interface AddItemToCartRequest {
    productId: number;
    quantity: number;
}

export interface UpdatedCartItemQuantityRequest {
    quantity: number;
}