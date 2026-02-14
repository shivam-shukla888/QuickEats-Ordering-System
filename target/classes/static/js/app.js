const API_BASE = '/api';

// State
let cart = JSON.parse(localStorage.getItem('quickeats_cart')) || [];

// DOM Elements
const cartCount = document.getElementById('cart-count');

// Init
document.addEventListener('DOMContentLoaded', () => {
    updateCartCount();

    // Router-like logic based on page
    if (document.getElementById('restaurant-list')) {
        loadRestaurants();
    } else if (document.getElementById('restaurant-menu')) {
        loadRestaurantDetails();
    } else if (document.getElementById('cart-items')) {
        loadCart();
    }
});

function updateCartCount() {
    if (cartCount) {
        cartCount.textContent = cart.reduce((acc, item) => acc + item.quantity, 0);
    }
    localStorage.setItem('quickeats_cart', JSON.stringify(cart));
}

// Fetch Restaurants
async function loadRestaurants() {
    try {
        const response = await fetch(`${API_BASE}/restaurants`);
        const restaurants = await response.json();
        const container = document.getElementById('restaurant-list');

        container.innerHTML = restaurants.map(r => `
            <div class="card">
                <img src="https://source.unsplash.com/400x300/?restaurant,food" class="card-img" alt="${r.name}">
                <div class="card-content">
                    <h3 class="card-title">${r.name}</h3>
                    <p class="card-text">${r.cuisineType} • ${r.address}</p>
                    <div class="card-footer">
                        <a href="restaurant.html?id=${r.id}" class="btn">View Menu</a>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (e) {
        console.error('Error loading restaurants:', e);
    }
}

// Fetch Restaurant Details
async function loadRestaurantDetails() {
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('id');

    if (!id) return;

    try {
        // Fetch info
        const rResponse = await fetch(`${API_BASE}/restaurants/${id}`);
        const restaurant = await rResponse.json();

        document.getElementById('restaurant-name').textContent = restaurant.name;
        document.getElementById('restaurant-info').textContent = `${restaurant.cuisineType} • ${restaurant.address}`;

        // Fetch menu
        const mResponse = await fetch(`${API_BASE}/restaurants/${id}/menu`);
        const menu = await mResponse.json();

        const container = document.getElementById('menu-list');
        container.innerHTML = menu.map(item => `
            <div class="menu-item">
                <div class="menu-info">
                    <h3>${item.itemName}</h3>
                    <p>${item.description || 'Delicious details coming soon'}</p>
                    <span class="price">$${item.price.toFixed(2)}</span>
                </div>
                <button class="btn" onclick="addToCart(${item.id}, '${item.itemName}', ${item.price}, ${restaurant.id}, '${restaurant.name}')">Add</button>
            </div>
        `).join('');

    } catch (e) {
        console.error('Error loading menu:', e);
    }
}

function addToCart(id, name, price, restaurantId, restaurantName) {
    const existing = cart.find(item => item.id === id);
    if (existing) {
        existing.quantity += 1;
    } else {
        cart.push({ id, name, price, restaurantId, restaurantName, quantity: 1 });
    }
    updateCartCount();
    alert('Added to cart!');
}

async function loadCart() {
    const container = document.getElementById('cart-items');
    const totalEl = document.getElementById('cart-total-amount');

    if (cart.length === 0) {
        container.innerHTML = '<div class="empty-state">Your cart is empty</div>';
        totalEl.textContent = '0.00';
        return;
    }

    container.innerHTML = cart.map(item => `
        <div class="cart-item">
            <div>
                <h4>${item.name}</h4>
                <p class="text-sm text-gray-500">${item.restaurantName} x ${item.quantity}</p>
            </div>
            <div class="flex items-center gap-4">
                <span class="font-bold">$${(item.price * item.quantity).toFixed(2)}</span>
                <button class="text-red-500 hover:text-red-700" onclick="removeFromCart(${item.id})">Remove</button>
            </div>
        </div>
    `).join('');

    const total = cart.reduce((acc, item) => acc + (item.price * item.quantity), 0);
    totalEl.textContent = total.toFixed(2);
}

function removeFromCart(id) {
    cart = cart.filter(item => item.id !== id);
    updateCartCount();
    loadCart();
}

async function placeOrder() {
    if (cart.length === 0) return alert('Cart is empty!');

    const user = JSON.parse(localStorage.getItem('user'));
    if (!user) {
        alert('Please login to place an order');
        window.location.href = 'login.html';
        return;
    }

    const ordersByRestaurant = {};
    cart.forEach(item => {
        if (!ordersByRestaurant[item.restaurantId]) {
            ordersByRestaurant[item.restaurantId] = {
                restaurant: { id: item.restaurantId },
                user: { id: user.id },
                items: [],
                totalAmount: 0
            };
        }
        ordersByRestaurant[item.restaurantId].items.push(item);
        ordersByRestaurant[item.restaurantId].totalAmount += item.price * item.quantity;
    });

    try {
        let lastOrderId;
        for (const rId in ordersByRestaurant) {
            const orderData = ordersByRestaurant[rId];
            orderData.orderItems = JSON.stringify(orderData.items);

            const response = await fetch(`${API_BASE}/orders`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(orderData)
            });
            const result = await response.json();
            lastOrderId = result.id;
        }

        alert('Order placed successfully!');
        cart = [];
        updateCartCount();
        window.location.href = `track.html?id=${lastOrderId}`;

    } catch (e) {
        console.error('Error placing order:', e);
        alert('Failed to place order.');
    }
}
