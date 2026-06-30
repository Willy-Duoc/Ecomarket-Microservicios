package com.ecomarket.catalogoinventario.config;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ecomarket.catalogoinventario.model.CategoriaProducto;
import com.ecomarket.catalogoinventario.model.EstadoProducto;
import com.ecomarket.catalogoinventario.model.Producto;
import com.ecomarket.catalogoinventario.repository.ProductoRepository;

@Component
public class CargaDatosIniciales implements CommandLineRunner{
    
    private static final Logger log = LoggerFactory.getLogger(CargaDatosIniciales.class);

    private final ProductoRepository productoRepository;

    public CargaDatosIniciales(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (productoRepository.count() > 0) {
            log.info("Catálogo ya inicializado ({} productos). No se siembra de nuevo.",
                    productoRepository.count());
            return;
        }
        sembrar();
        log.info("Catálogo inicializado con {} productos.", productoRepository.count());
    }

    private void sembrar() {
        // 10 categorías × 10 productos = 100. Orden fijo → IDs estables 1..100.
        CategoriaSemilla[] categorias = {
            new CategoriaSemilla(CategoriaProducto.ALIMENTOS_ORGANICOS, "ALI", new String[]{
                "Quinoa Orgánica", "Miel Pura de Abeja", "Aceite de Oliva Extra Virgen",
                "Café de Grano Orgánico", "Arroz Integral Orgánico", "Lentejas Orgánicas",
                "Granola Artesanal", "Té Verde Orgánico", "Azúcar de Coco", "Harina Integral Orgánica"}),
            new CategoriaSemilla(CategoriaProducto.LIMPIEZA_ECOLOGICA, "LIM", new String[]{
                "Detergente Biodegradable", "Jabón de Castilla", "Limpiador Multiuso Natural",
                "Vinagre de Limpieza", "Bicarbonato Ecológico", "Esponja Vegetal",
                "Desinfectante Natural", "Quitamanchas Ecológico", "Lavaloza Concentrado", "Suavizante Natural"}),
            new CategoriaSemilla(CategoriaProducto.PRODUCTOS_REUTILIZABLES, "REU", new String[]{
                "Botella de Acero Inoxidable", "Bolsa de Tela Reutilizable", "Pajita de Bambú",
                "Envoltorio de Cera de Abeja", "Taza de Bambú", "Cubiertos de Bambú",
                "Bolsa de Malla", "Film Reutilizable", "Contenedor de Vidrio", "Servilletas de Tela"}),
            new CategoriaSemilla(CategoriaProducto.HIGIENE_PERSONAL, "HIG", new String[]{
                "Cepillo de Dientes de Bambú", "Shampoo Sólido", "Jabón Natural Artesanal",
                "Desodorante Natural", "Pasta Dental Ecológica", "Toallas Reutilizables",
                "Copa Menstrual", "Crema Hidratante Natural", "Bálsamo Labial Orgánico", "Maquinilla de Afeitar Metálica"}),
            new CategoriaSemilla(CategoriaProducto.HOGAR_SOSTENIBLE, "HOG", new String[]{
                "Vela de Cera de Soja", "Bombilla LED Bajo Consumo", "Maceta Biodegradable",
                "Alfombra de Fibra Natural", "Cortina de Bambú", "Cojín de Algodón Orgánico",
                "Set de Toallas Orgánicas", "Difusor de Aromas Natural", "Cesta de Mimbre", "Reloj de Madera Reciclada"}),
            new CategoriaSemilla(CategoriaProducto.JARDINERIA, "JAR", new String[]{
                "Compostera Doméstica", "Semillas Orgánicas", "Tierra de Hojas",
                "Regadera Reciclada", "Guantes de Jardín Biodegradables", "Herramientas de Jardín de Bambú",
                "Humus de Lombriz", "Maceteros Reciclados", "Sustrato Ecológico", "Insecticida Natural"}),
            new CategoriaSemilla(CategoriaProducto.AGRICULTURA, "AGR", new String[]{
                "Fertilizante Orgánico", "Malla Antimaleza Biodegradable", "Sistema de Riego por Goteo",
                "Semillero Reciclado", "Abono Verde", "Repelente Natural de Plagas",
                "Tutor de Bambú", "Acolchado Natural", "Cal Agrícola", "Trampa Ecológica de Insectos"}),
            new CategoriaSemilla(CategoriaProducto.MASCOTAS, "MAS", new String[]{
                "Alimento Orgánico para Perros", "Arena Biodegradable para Gatos", "Juguete Natural para Mascotas",
                "Shampoo Natural para Mascotas", "Cama Ecológica para Mascotas", "Correa de Cáñamo",
                "Snacks Orgánicos", "Comedero de Bambú", "Collar Natural Antipulgas", "Bolsas Biodegradables para Desechos"}),
            new CategoriaSemilla(CategoriaProducto.RECICLAJE, "REC", new String[]{
                "Set de Contenedores de Reciclaje", "Compactadora Doméstica", "Papelera de Cartón Reciclado",
                "Bolsas Compostables", "Separador de Residuos", "Cubo de Reciclaje Plegable",
                "Etiquetas de Reciclaje", "Contenedor de Vidrio para Reciclaje", "Trituradora de Papel Manual", "Kit de Reciclaje Doméstico"}),
            new CategoriaSemilla(CategoriaProducto.ENERGIA_SOSTENIBLE, "ENE", new String[]{
                "Panel Solar Portátil", "Cargador Solar USB", "Batería Recargable Ecológica",
                "Lámpara Solar de Jardín", "Linterna de Manivela", "Powerbank Solar",
                "Termo Solar", "Regleta de Ahorro Energético", "Cargador Dinamo de Bicicleta", "Kit de Energía Solar Doméstico"})
        };

        for (int c = 0; c < categorias.length; c++) {
            CategoriaSemilla cat = categorias[c];
            for (int i = 0; i < cat.nombres().length; i++) {
                String numero = String.format("%02d", i + 1);
                String sku = cat.prefijo() + "-" + numero;
                // Precio determinista: base por categoría + incremento por índice.
                BigDecimal precio = new BigDecimal(1990 + (c * 1000) + (i * 150));

                Producto producto = Producto.builder()
                        .sku(sku)
                        .nombre(cat.nombres()[i])
                        .descripcion("Producto ecológico y sostenible: " + cat.nombres()[i])
                        .categoria(cat.categoria())
                        .precio(precio)
                        .stock(100)
                        .imagenUrl("https://ecomarket.cl/img/" + sku.toLowerCase() + ".jpg")
                        .estado(EstadoProducto.DISPONIBLE)
                        .build();

                productoRepository.save(producto);
            }
        }
    }

    /** Estructura auxiliar inmutable para describir la semilla de cada categoría. */
    private record CategoriaSemilla(CategoriaProducto categoria, String prefijo, String[] nombres) {
    }
}
