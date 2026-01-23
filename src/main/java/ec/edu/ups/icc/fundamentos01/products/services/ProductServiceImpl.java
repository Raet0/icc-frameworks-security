package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.categories.reporitory.CategoryRepository;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.models.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.repository.ProductRepository;
import ec.edu.ups.icc.fundamentos01.users.repository.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    public ProductServiceImpl(ProductRepository productRepo,
            UserRepository userRepo,
            CategoryRepository categoryRepository) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepository;
        this.userRepo = userRepo;
    }

    // ============== MÉTODOS EXISTENTES (IMPLEMENTADOS) ==============

    @Override
    public ProductResponseDto create(CreateProductDto dto) {
        // Validar que el usuario existe
        if (!userRepo.existsById(dto.userId)) {
            throw new NotFoundException("Usuario no encontrado con ID: " + dto.userId);
        }

        // Validar categorías
        List<CategoryEntity> categories = validateAndGetCategories(dto.categoryIds);

        // Crear entidad
        ProductEntity product = new ProductEntity();
        product.setName(dto.name);
        product.setPrice(dto.price);
        product.setDescription(dto.description);
        product.setOwner(userRepo.findById(dto.userId).orElseThrow());
        product.setCategories(new HashSet<>(categories));

        // Guardar y retornar DTO
        ProductEntity saved = productRepo.save(product);
        return toResponseDto(saved);
    }

    @Override
    public ProductResponseDto findById(Long id) {
        ProductEntity product = productRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));
        return toResponseDto(product);
    }

    @Override
    public ProductResponseDto update(Long id, UpdateProductDto dto) {
        ProductEntity product = productRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));

        // Actualizar campos
        if (dto.name != null) product.setName(dto.name);
        if (dto.price != null) product.setPrice(dto.price);
        if (dto.description != null) product.setDescription(dto.description);
        if (dto.categoryIds != null) {
            product.setCategories(new HashSet<>(validateAndGetCategories(dto.categoryIds)));
        }

        ProductEntity updated = productRepo.save(product);
        return toResponseDto(updated);
    }

    @Override
    public void delete(Long id) {
        if (!productRepo.existsById(id)) {
            throw new NotFoundException("Producto no encontrado con ID: " + id);
        }
        productRepo.deleteById(id);
    }

    @Override
    public List<ProductResponseDto> findAll() {
        List<ProductEntity> products = productRepo.findAll();
        return products.stream().map(this::toResponseDto).toList();
    }

    @Override
    public List<ProductResponseDto> findByUserId(Long id) {
        if (!userRepo.existsById(id)) {
            throw new NotFoundException("Usuario no encontrado con ID: " + id);
        }
        List<ProductEntity> products = productRepo.findByOwnerId(id);
        return products.stream().map(this::toResponseDto).toList();
    }

    @Override
    public List<ProductResponseDto> findByCategoryId(Long id) {
        if (!categoryRepo.existsById(id)) {
            throw new NotFoundException("Categoría no encontrada con ID: " + id);
        }
        List<ProductEntity> products = productRepo.findByCategoriesId(id);
        return products.stream().map(this::toResponseDto).toList();
    }

    // ============== MÉTODOS CON PAGINACIÓN ==============

    @Override
    public Page<ProductResponseDto> findAll(int page, int size, String[] sort) {
        Pageable pageable = createPageable(page, size, sort);
        Page<ProductEntity> productPage = productRepo.findAll(pageable);
        
        return productPage.map(this::toResponseDto);
    }

    @Override
    public Slice<ProductResponseDto> findAllSlice(int page, int size, String[] sort) {
        Pageable pageable = createPageable(page, size, sort);
        Slice<ProductEntity> productSlice = productRepo.findAll(pageable);
        
        return productSlice.map(this::toResponseDto);
    }

    @Override
    public Page<ProductResponseDto> findWithFilters(
            String name, Double minPrice, Double maxPrice, Long categoryId,
            int page, int size, String[] sort) {
        
        // Validaciones de filtros (del tema 09)
        validateFilterParameters(minPrice, maxPrice);
        
        // Crear Pageable
        Pageable pageable = createPageable(page, size, sort);
        
        // Consulta con filtros y paginación
        Page<ProductEntity> productPage = productRepo.findWithFilters(
            name, minPrice, maxPrice, categoryId, pageable);
        
        return productPage.map(this::toResponseDto);
    }

    @Override
    public Page<ProductResponseDto> findByUserIdWithFilters(
            Long userId, String name, Double minPrice, Double maxPrice, Long categoryId,
            int page, int size, String[] sort) {
        
        // 1. Validar que el usuario existe
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException("Usuario no encontrado con ID: " + userId);
        }
        
        // 2. Validar filtros
        validateFilterParameters(minPrice, maxPrice);
        
        // 3. Crear Pageable
        Pageable pageable = createPageable(page, size, sort);
        
        // 4. Consulta con filtros y paginación
        Page<ProductEntity> productPage = productRepo.findByUserIdWithFilters(
            userId, name, minPrice, maxPrice, categoryId, pageable);
        
        return productPage.map(this::toResponseDto);
    }

    // ============== MÉTODOS HELPER ==============

    private Pageable createPageable(int page, int size, String[] sort) {
        // Validar parámetros
        if (page < 0) {
            throw new BadRequestException("La página debe ser mayor o igual a 0");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("El tamaño debe estar entre 1 y 100");
        }
        
        // Crear Sort
        Sort sortObj = createSort(sort);
        
        return PageRequest.of(page, size, sortObj);
    }

    private Sort createSort(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.by("id");
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (String sortParam : sort) {
            String[] parts = sortParam.split(",");
            String property = parts[0];
            String direction = parts.length > 1 ? parts[1] : "asc";
            
            // Validar propiedades permitidas para evitar inyección SQL
            if (!isValidSortProperty(property)) {
                throw new BadRequestException("Propiedad de ordenamiento no válida: " + property);
            }
            
            Sort.Order order = "desc".equalsIgnoreCase(direction) 
                ? Sort.Order.desc(property)
                : Sort.Order.asc(property);
            
            orders.add(order);
        }
        
        return Sort.by(orders);
    }

    private boolean isValidSortProperty(String property) {
        // Lista blanca de propiedades permitidas para ordenamiento
        Set<String> allowedProperties = Set.of(
            "id", "name", "price", "createdAt", "updatedAt",
            "owner.name", "owner.email", "category.name"
        );
        return allowedProperties.contains(property);
    }

    private void validateFilterParameters(Double minPrice, Double maxPrice) {
        if (minPrice != null && minPrice < 0) {
            throw new BadRequestException("El precio mínimo no puede ser negativo");
        }
        
        if (maxPrice != null && maxPrice < 0) {
            throw new BadRequestException("El precio máximo no puede ser negativo");
        }
        
        if (minPrice != null && maxPrice != null && maxPrice < minPrice) {
            throw new BadRequestException("El precio máximo debe ser mayor o igual al precio mínimo");
        }
    }

    private List<CategoryEntity> validateAndGetCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos una categoría");
        }
        List<CategoryEntity> categories = new ArrayList<>();
        for (Long id : categoryIds) {
            CategoryEntity category = categoryRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con ID: " + id));
            categories.add(category);
        }
        return categories;
    }

    private ProductResponseDto toResponseDto(ProductEntity product) {
        ProductResponseDto dto = new ProductResponseDto();
        
        dto.id = product.getId();
        dto.name = product.getName();
        dto.price = product.getPrice();
        dto.description = product.getDescription();
        dto.createdAt = product.getCreatedAt();
        dto.updatedAt = product.getUpdatedAt();
        
        // Información del usuario (owner)
        dto.user = new ProductResponseDto.UserSummaryDto();
        dto.user.id = product.getOwner().getId();
        dto.user.name = product.getOwner().getName();
        dto.user.email = product.getOwner().getEmail();
        
        // Información de las categorías (ajustado para múltiples categorías)
        List<CategoryResponseDto> categoryDtos = new ArrayList<>();
        for (CategoryEntity categoryEntity : product.getCategories()) {
            CategoryResponseDto categoryDto = new CategoryResponseDto();
            categoryDto.id = categoryEntity.getId();
            categoryDto.name = categoryEntity.getName();
            categoryDto.description = categoryEntity.getDescription();
            categoryDtos.add(categoryDto);
        }
        dto.categories = categoryDtos;
        
        return dto;
    }
}