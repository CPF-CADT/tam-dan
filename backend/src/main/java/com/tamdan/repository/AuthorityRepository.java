package com.tamdan.repository;

import com.tamdan.entities.Authority;
import org.springframework.data.jpa.repository.*;

public interface AuthorityRepository extends JpaRepository<Authority, String> {}
