import psycopg2
from faker import Faker
import random
from datetime import datetime

# Configuración de la base de datos
DB_CONFIG = {
    "dbname": "devdb",
    "user": "ups",
    "password": "ups123",
    "host": "localhost",
    "port": "5432"
}

fake = Faker()

def seed_database():
    conn = None
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()
        print("Conectado a la base de datos...")

        # 1. Insertar Categorías
        print("Insertando categorías...")
        categories = ['Electrónica', 'Hogar', 'Deportes', 'Libros', 'Juguetes', 'Ropa', 'Jardín', 'Mascotas']
        category_ids = []
        for cat_name in categories:
            cur.execute(
                "INSERT INTO categories (name, description, deleted, created_at, updated_at) "
                "VALUES (%s, %s, %s, %s, %s) RETURNING id",
                (cat_name, fake.sentence(), False, datetime.now(), datetime.now())
            )
            category_ids.append(cur.fetchone()[0])

        # 2. Insertar Usuarios
        print("Insertando 10 usuarios...")
        user_ids = []
        for _ in range(10):
            cur.execute(
                "INSERT INTO users (name, email, password, deleted, created_at, updated_at) "
                "VALUES (%s, %s, %s, %s, %s, %s) RETURNING id",
                (fake.name(), fake.unique.email(), 'password123', False, datetime.now(), datetime.now())
            )
            user_ids.append(cur.fetchone()[0])

        # 3. Insertar 1000 Productos
        print("Insertando 1000 productos...")
        product_ids = []
        for _ in range(1000):
            price = round(random.uniform(5.0, 2000.0), 2)
            cur.execute(
                "INSERT INTO products (name, price, description, user_id, deleted, created_at, updated_at) "
                "VALUES (%s, %s, %s, %s, %s, %s, %s) RETURNING id",
                (fake.catch_phrase(), price, fake.text(max_nb_chars=200), random.choice(user_ids), False, datetime.now(), datetime.now())
            )
            product_ids.append(cur.fetchone()[0])

        # 4. Relacionar Productos con Categorías (Tabla Many-to-Many)
        print("Relacionando productos con categorías...")
        for p_id in product_ids:
            # Cada producto tendrá entre 1 y 2 categorías
            chosen_cats = random.sample(category_ids, k=random.randint(1, 2))
            for c_id in chosen_cats:
                cur.execute(
                    "INSERT INTO product_categories (product_id, category_id) VALUES (%s, %s)",
                    (p_id, c_id)
                )

        conn.commit()
        print("¡Proceso completado exitosamente!")

    except Exception as e:
        if conn:
            conn.rollback()
        print(f"Error durante la carga: {e}")
    finally:
        if conn:
            cur.close()
            conn.close()

if __name__ == "__main__":
    seed_database()