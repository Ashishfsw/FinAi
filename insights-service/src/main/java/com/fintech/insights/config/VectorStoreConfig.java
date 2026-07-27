package com.fintech.insights.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.ai.vectorstore.pgvector.index-type:ivfflat}")
    private String indexType;

    @Value("${spring.ai.vectorstore.pgvector.dimension:1536}")
    private int dimension;

    @Value("${spring.ai.vectorstore.pgvector.distance-type:cosine}")
    private String distanceType;

    @Value("${spring.ai.vectorstore.pgvector.create-extension:true}")
    private boolean createExtension;

    @Bean
    public VectorStore vectorStore(DataSource dataSource, EmbeddingModel embeddingModel, JdbcTemplate jdbcTemplate) {
        // Ensure pgvector extension is installed
        if (createExtension) {
            try {
                jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            } catch (Exception e) {
                // Extension might already exist or insufficient permissions
                // Log but don't fail the bean creation
                System.out.println("Note: Could not create vector extension: " + e.getMessage());
            }
        }

        // Create the vector store table if it doesn't exist
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS vector_store (
                    id SERIAL PRIMARY KEY,
                    content TEXT NOT NULL,
                    metadata JSONB,
                    embedding vector(1536),
                    similarity_score FLOAT
                );
                """);
            
            // Create index for similarity search
            jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS vector_store_embedding_idx 
                ON vector_store 
                USING ivfflat (embedding vector_cosine_ops)
                WITH (lists = 100);
                """);
        } catch (Exception e) {
            System.out.println("Note: Could not create vector store table/index: " + e.getMessage());
        }

        return new PgVectorStore(dataSource, embeddingModel, jdbcTemplate, indexType, dimension, distanceType, true);
    }
}
