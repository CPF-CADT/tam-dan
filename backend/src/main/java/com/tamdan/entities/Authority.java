package com.tamdan.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Getter
@Builder
@Entity
@Table(name = "jhi_authority")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(value = { "new", "id" })
public class Authority implements Serializable, Persistable<String> {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Size(max = 50)
    @Id
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean persisted;

    public static Authority named(String name) {
        return Authority.builder().name(name).build();
    }

    @PostLoad
    @PostPersist
    void markPersisted() {
        this.persisted = true;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public boolean isNew() {
        return !persisted;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof Authority authority && name != null && name.equals(authority.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "Authority{name='" + name + "'}";
    }
}
