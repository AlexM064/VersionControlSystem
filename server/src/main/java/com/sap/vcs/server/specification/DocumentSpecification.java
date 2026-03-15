package com.sap.vcs.server.specification;

import com.sap.vcs.server.entity.Document;
import com.sap.vcs.server.entity.enums.DocumentStatus;
import org.springframework.data.jpa.domain.Specification;

public class DocumentSpecification {

    public static Specification<Document> hasStatus(DocumentStatus status) {
        return (root, query, builder) ->
                status == null ? null : builder.equal(root.get("status"), status);
    }

    public static Specification<Document> titleContains(String title) {
        return (root, query, builder) ->
                title == null ? null : builder.like(
                        builder.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%"
                );
    }
}