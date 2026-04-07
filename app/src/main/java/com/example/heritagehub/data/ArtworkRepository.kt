package com.example.heritagehub.data

import com.example.heritagehub.model.Artwork
import kotlinx.coroutines.delay

class ArtworkRepository {
    suspend fun getArtworks(): List<Artwork> {
        delay(1000)
        return listOf(
            Artwork("1", "Ceramic Vase", "Maria Santos", "https://images.unsplash.com/photo-1578500494198-246f612d03b3?w=300"),
            Artwork("2", "Handwoven Basket", "Amara Okonkwo", "https://images.unsplash.com/photo-1584308666744-24d5f00206dd?w=300"),
            Artwork("3", "Wood Carving", "Chen Wei", "https://images.unsplash.com/photo-1578926078328-123456789abc?w=300"),
            Artwork("4", "Batik Cloth", "Siti Rahayu", "https://images.unsplash.com/photo-1578926078456-123456789abd?w=300"),
            Artwork("5", "Silver Jewelry", "Alex Vasquez", "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=300"),
            Artwork("6", "Hand Painted Tiles", "Laila Hassan", "https://images.unsplash.com/photo-1578926078567-123456789abe?w=300"),
            Artwork("7", "Macramé Wall Hanging", "James Thompson", "https://images.unsplash.com/photo-1578926078678-123456789abf?w=300"),
            Artwork("8", "Stone Sculpture", "Kwame Mensah", "https://images.unsplash.com/photo-1578926078789-123456789ab0?w=300"),
            Artwork("9", "Embroidered Textile", "Rosa Garcia", "https://images.unsplash.com/photo-1578926078890-123456789ab1?w=300"),
            Artwork("10", "Leatherwork Bag", "Marco Rossi", "https://images.unsplash.com/photo-1578926078901-123456789ab2?w=300"),
            Artwork("11", "Glass Sculpture", "Yuki Tanaka", "https://images.unsplash.com/photo-1578926078012-123456789ab3?w=300"),
            Artwork("12", "Wooden Frame Art", "Oluwaseun Adebayo", "https://images.unsplash.com/photo-1578926079123-123456789ab4?w=300")
        )
    }
}

