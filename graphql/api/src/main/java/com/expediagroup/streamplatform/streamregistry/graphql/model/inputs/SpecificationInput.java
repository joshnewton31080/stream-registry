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
package com.expediagroup.streamplatform.streamregistry.graphql.model.inputs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Value;

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.expediagroup.streamplatform.streamregistry.model.Principal;
import com.expediagroup.streamplatform.streamregistry.model.Security;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Tag;

@Value
@Builder
public class SpecificationInput {
  String description;
  List<TagInput> tags;
  String type;
  ObjectNode configuration;
  List<SecurityInput> security;
  String function;

  private static List<Tag> getTags(List<TagInput> input) {
    List<Tag> out = new ArrayList<>();
    if (input != null) {
      for (TagInput t : input) {
        out.add(new Tag(t.getName(), t.getValue()));
      }
    }
    return out;
  }

  private static List<Security> getSecurity(List<SecurityInput> input) {
    if (null == input)
      return Collections.emptyList();

    return input.stream().map(si -> new Security(
      si.getRole(),
      si.getPrincipals().stream().map(pi -> new Principal(pi.getName())).collect(Collectors.toList())
    )).collect(Collectors.toList());
  }

  public Specification asSpecification() {
    return new Specification(
        description,
        getTags(tags),
        type,
        configuration,
        getSecurity(security),
        function
    );
  }
}
