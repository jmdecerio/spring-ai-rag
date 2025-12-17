package guru.springframework.springairag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    ChatClient chatClient(ObjectProvider<ChatClient.Builder> builderProvider,
                          VectorStore vectorStore) {

        // Builder "limpio" SOLO para transformers (no se toca después)
        ChatClient.Builder transformerBuilder = builderProvider.getObject();

        // Builder separado para el ChatClient final con el advisor
        ChatClient.Builder ragBuilder = builderProvider.getObject();

        var advisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .build())
                .queryTransformers(
                        TranslationQueryTransformer.builder()
                                .chatClientBuilder(transformerBuilder)
                                .targetLanguage("English")
                                .build(),
                        RewriteQueryTransformer.builder()
                                .chatClientBuilder(transformerBuilder)
                                .build())
                .build();

        return ragBuilder
                .defaultAdvisors(advisor)
                .build();
    }
}