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
package com.expediagroup.streamplatform.streamregistry.core.validators;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.views.StreamBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;

@Component
@RequiredArgsConstructor
public class ProcessBindingValidator implements Validator<ProcessBinding> {
  private final StreamBindingView streamBindingView;
  private final ZoneView zoneView;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(ProcessBinding processBinding) throws ValidationException {
    validateForCreateAndUpdate(processBinding);
    specificationValidator.validateForCreate(processBinding.getSpecification());
  }

  @Override
  public void validateForUpdate(ProcessBinding processBinding, ProcessBinding existing) throws ValidationException {
    validateForCreateAndUpdate(processBinding);
    specificationValidator.validateForUpdate(processBinding.getSpecification(), existing.getSpecification());
  }

  public void validateForCreateAndUpdate(ProcessBinding processBinding) throws ValidationException {
    if (!zoneView.exists(processBinding.getZone())) {
      throw new ValidationException("Zone [" + processBinding.getZone() + "] does not exist");
    }

    processBinding.getInputs().forEach(input -> {
      if (!streamBindingView.exists(input.getStreamBindingKey())) {
        throw new ValidationException("Input StreamBinding Key [" + input.getStreamBindingKey() + "] does not exist");
      }
    });

    processBinding.getOutputs().forEach(output -> {
      if (!streamBindingView.exists(output.getStreamBindingKey())) {
        throw new ValidationException("Output StreamBinding Key [" + output.getStreamBindingKey() + "] does not exist");
      }
    });
  }

}
