package models;

import generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileNameRequest extends BaseModel {

    @GeneratingRule(regex = "[A-Za-z]{5,15} [A-Za-z]{5,15}")
    private String name;
}