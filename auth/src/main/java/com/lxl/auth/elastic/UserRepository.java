package com.lxl.auth.elastic;

import com.lxl.common.entity.auth.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserRepository extends ElasticsearchRepository<User, Long> {
}
