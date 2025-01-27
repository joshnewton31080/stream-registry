/**
 * Copyright (C) 2018-2021 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.core.services;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProducerValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerView;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerRepository;

@Component
@RequiredArgsConstructor
public class ProducerService {
  private final HandlerService handlerService;
  private final ProducerValidator producerValidator;
  private final ProducerRepository producerRepository;
  private final ProducerBindingService producerBindingService;
  private final ProducerBindingView producerBindingView;
  private final ProducerView producerView;

  @PreAuthorize("hasPermission(#producer, 'CREATE')")
  public Optional<Producer> create(Producer producer) throws ValidationException {
    if (producerView.get(producer.getKey()).isPresent()) {
      throw new ValidationException("Can't create " + producer.getKey() + " because it already exists");
    }
    producerValidator.validateForCreate(producer);
    producer.setSpecification(handlerService.handleInsert(producer));
    return save(producer);
  }

  @PreAuthorize("hasPermission(#producer, 'UPDATE')")
  public Optional<Producer> update(Producer producer) throws ValidationException {
    val existing = producerView.get(producer.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + producer.getKey().getName() + " because it doesn't exist");
    }
    producerValidator.validateForUpdate(producer, existing.get());
    producer.setSpecification(handlerService.handleUpdate(producer, existing.get()));
    return save(producer);
  }

  @PreAuthorize("hasPermission(#producer, 'UPDATE_STATUS')")
  public Optional<Producer> updateStatus(Producer producer, Status status) {
    producer.setStatus(status);
    return save(producer);
  }

  private Optional<Producer> save(Producer producer) {
    return Optional.ofNullable(producerRepository.save(producer));
  }

  @PostAuthorize("returnObject.isPresent() ? hasPermission(returnObject, 'READ') : true")
  public Optional<Producer> get(ProducerKey key) {
    return producerView.get(key);
  }

  @PostFilter("hasPermission(filterObject, 'READ')")
  public List<Producer> findAll(Predicate<Producer> filter) {
    return producerView.findAll(filter).collect(toList());
  }

  @PreAuthorize("hasPermission(#producer, 'DELETE')")
  public void delete(Producer producer) {
    handlerService.handleDelete(producer);
    producerBindingView
      .findAll(b -> b.getKey().getProducerKey().equals(producer.getKey()))
      .forEach(producerBindingService::delete);
    producerRepository.delete(producer);
  }

  @PreAuthorize("hasPermission(#producer, 'CREATE')")
  public Producer canCreateProducer(Producer producer) {
    return producer;
  }

  @PreAuthorize("hasPermission(#producer, 'UPDATE')")
  public Producer canUpdateProducer(Producer producer) {
    return producer;
  }

  @PreAuthorize("hasPermission(#producer, 'DELETE')")
  public Producer canDeleteProducer(Producer producer) {
    return producer;
  }

}
