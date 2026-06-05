package com.binarylabyrinth.productservice.config;

import com.binarylabyrinth.message.ProductCreatedEvent;
import com.binarylabyrinth.productservice.entity.Product;
import com.binarylabyrinth.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DataSeeder — seeds demo product catalog into MongoDB on first boot.
 *
 * Runs once at startup. If the products collection already has documents
 * (i.e. the DB was populated before) this is a no-op — it will never
 * overwrite or duplicate existing data.
 *
 * After inserting each product a ProductCreatedEvent is published to Kafka
 * so the inventory-service's ProductCreatedConsumer can register an initial
 * inventory row for each product automatically.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) {
            log.info("Products collection already populated — skipping seed.");
            return;
        }

        log.info("Seeding demo product catalog…");
        List<Product> demos = demoProducts();
        List<Product> saved = productRepository.saveAll(demos);

        saved.forEach(p -> {
            try {
                kafkaTemplate.send("product-created",
                        ProductCreatedEvent.builder()
                                .productId(p.getId())
                                .productName(p.getName())
                                .price(p.getPrice())
                                .stock(p.getStock())
                                .createdAt(p.getCreatedAt())
                                .build());
            } catch (Exception ex) {
                log.warn("Could not publish ProductCreatedEvent for {}: {}", p.getId(), ex.getMessage());
            }
        });

        log.info("Seeded {} demo products.", saved.size());
    }

    private static List<Product> demoProducts() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(

            Product.builder()
                .name("Sony WH-1000XM5 Wireless Headphones")
                .description("Industry-leading noise cancellation with 30-hour battery life. Multipoint connection lets you pair with two devices simultaneously. Foldable design for easy portability.")
                .price(349.99)
                .category("Electronics")
                .brand("Sony")
                .sku("SONY-WH1000XM5")
                .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400")
                .tags(List.of("headphones", "wireless", "noise-cancelling", "audio"))
                .stock(50)
                .createdAt(now)
                .build(),

            Product.builder()
                .name("Keychron K2 Mechanical Keyboard")
                .description("Compact 75% layout wireless mechanical keyboard with RGB backlighting. Compatible with Mac and Windows. Hot-swappable switches for easy customisation.")
                .price(89.99)
                .category("Electronics")
                .brand("Keychron")
                .sku("KEYCHRON-K2")
                .imageUrl("https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=400")
                .tags(List.of("keyboard", "mechanical", "wireless", "gaming"))
                .stock(75)
                .createdAt(now)
                .build(),

            Product.builder()
                .name("Anker 12-in-1 USB-C Hub")
                .description("Expand your laptop with HDMI 4K output, 3 USB-A 3.0 ports, SD/microSD card readers, 100W PD charging, Ethernet, and more. Plug-and-play, no drivers needed.")
                .price(59.99)
                .category("Accessories")
                .brand("Anker")
                .sku("ANKER-USB-HUB-12")
                .imageUrl("https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=400")
                .tags(List.of("usb-c", "hub", "dongle", "accessories", "laptop"))
                .stock(120)
                .createdAt(now)
                .build(),

            Product.builder()
                .name("Logitech MX Master 3S Mouse")
                .description("Advanced wireless mouse with ultra-fast MagSpeed scrolling, 8K DPI sensor, quiet clicks, and ergonomic design. Works on any surface including glass.")
                .price(99.99)
                .category("Electronics")
                .brand("Logitech")
                .sku("LOGI-MX-MASTER-3S")
                .imageUrl("https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400")
                .tags(List.of("mouse", "wireless", "ergonomic", "productivity"))
                .stock(90)
                .createdAt(now)
                .build(),

            Product.builder()
                .name("JBL Charge 5 Bluetooth Speaker")
                .description("Portable Bluetooth speaker with powerful sound and deep bass. IP67 waterproof and dustproof. 20 hours of playtime. Built-in powerbank charges your devices.")
                .price(179.95)
                .category("Audio")
                .brand("JBL")
                .sku("JBL-CHARGE5")
                .imageUrl("https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400")
                .tags(List.of("speaker", "bluetooth", "waterproof", "portable", "audio"))
                .stock(60)
                .createdAt(now)
                .build(),

            Product.builder()
                .name("Apple AirPods Pro (2nd Gen)")
                .description("Active noise cancellation, Adaptive Transparency mode, Personalized Spatial Audio. Up to 6 hours of listening time (30 hours with case). MagSafe charging case.")
                .price(249.00)
                .category("Audio")
                .brand("Apple")
                .sku("APPLE-AIRPODS-PRO-2")
                .imageUrl("https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=400")
                .tags(List.of("earbuds", "wireless", "noise-cancelling", "apple", "audio"))
                .stock(40)
                .createdAt(now)
                .build(),

            Product.builder()
                .name("Samsung Galaxy Watch 6")
                .description("Advanced health monitoring with body composition, continuous heart rate, sleep coaching, and ECG. 40mm AMOLED display. Wear OS with Google apps.")
                .price(299.99)
                .category("Wearables")
                .brand("Samsung")
                .sku("SAMSUNG-GW6-40MM")
                .imageUrl("https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=400")
                .tags(List.of("smartwatch", "wearable", "fitness", "health", "samsung"))
                .stock(35)
                .createdAt(now)
                .build(),

            Product.builder()
                .name("Fitbit Charge 6 Fitness Tracker")
                .description("Built-in GPS, heart rate monitoring, stress management, sleep tracking, and 40+ exercise modes. Google Maps and Google Wallet on your wrist. 7-day battery.")
                .price(159.95)
                .category("Wearables")
                .brand("Fitbit")
                .sku("FITBIT-CHARGE6")
                .imageUrl("https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6?w=400")
                .tags(List.of("fitness", "tracker", "wearable", "health", "gps"))
                .stock(80)
                .createdAt(now)
                .build(),

            Product.builder()
                .name("Twelve South HiRise Pro Laptop Stand")
                .description("Premium adjustable aluminium laptop stand raises your screen to eye level, improving posture. Compatible with MacBooks and most 13-16\" laptops. Folds flat for travel.")
                .price(79.99)
                .category("Accessories")
                .brand("Twelve South")
                .sku("12S-HIRISE-PRO")
                .imageUrl("https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400")
                .tags(List.of("laptop", "stand", "desk", "ergonomic", "accessories"))
                .stock(55)
                .createdAt(now)
                .build(),

            Product.builder()
                .name("Elgato Stream Deck MK.2")
                .description("15 customisable LCD keys to control streaming apps, switch scenes, adjust audio, and trigger actions. Interchangeable faceplates. Works with OBS, Twitch, YouTube, and more.")
                .price(149.99)
                .category("Electronics")
                .brand("Elgato")
                .sku("ELGATO-STREAMDECK-MK2")
                .imageUrl("https://images.unsplash.com/photo-1561070791-2526d30994b5?w=400")
                .tags(List.of("streaming", "content-creation", "gaming", "productivity"))
                .stock(45)
                .createdAt(now)
                .build(),

            Product.builder()
                .name("Belkin MagSafe 3-in-1 Wireless Charger")
                .description("Charge iPhone, Apple Watch, and AirPods simultaneously. MagSafe alignment ensures maximum 15W wireless charging speed. Compact, cable-management design for your desk.")
                .price(149.99)
                .category("Accessories")
                .brand("Belkin")
                .sku("BELKIN-MAGSAFE-3IN1")
                .imageUrl("https://images.unsplash.com/photo-1586953208448-b95a79798f07?w=400")
                .tags(List.of("charger", "wireless", "magsafe", "apple", "accessories"))
                .stock(100)
                .createdAt(now)
                .build(),

            Product.builder()
                .name("Razer DeathAdder V3 Gaming Mouse")
                .description("Ergonomic wired gaming mouse with 30K optical sensor, optical switches rated for 90M clicks, Focus Pro sensor, and ultra-lightweight 59g design for competitive play.")
                .price(69.99)
                .category("Electronics")
                .brand("Razer")
                .sku("RAZER-DA-V3")
                .imageUrl("https://images.unsplash.com/photo-1605773527852-c546a8584ea3?w=400")
                .tags(List.of("gaming", "mouse", "esports", "wired"))
                .stock(65)
                .createdAt(now)
                .build()
        );
    }
}
