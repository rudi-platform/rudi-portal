package org.rudi.facet.rva.impl.rva.bean;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

/**
 * FullAddressesResponseRva
 */

@JsonTypeName("FullAddressesResponse_rva")
public class FullAddressesResponseRva {

	private String request;

	private FullAddressesAnswer answer;

	public FullAddressesResponseRva request(String request) {
		this.request = request;
		return this;
	}

	/**
	 * Get request
	 * 
	 * @return request
	 */

	@Schema(name = "request", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("request")
	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public FullAddressesResponseRva answer(FullAddressesAnswer answer) {
		this.answer = answer;
		return this;
	}

	/**
	 * Get answer
	 * 
	 * @return answer
	 */
	@Valid
	@Schema(name = "answer", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("answer")
	public FullAddressesAnswer getAnswer() {
		return answer;
	}

	public void setAnswer(FullAddressesAnswer answer) {
		this.answer = answer;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FullAddressesResponseRva fullAddressesResponseRva = (FullAddressesResponseRva) o;
		return Objects.equals(this.request, fullAddressesResponseRva.request)
				&& Objects.equals(this.answer, fullAddressesResponseRva.answer);
	}

	@Override
	public int hashCode() {
		return Objects.hash(request, answer);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class FullAddressesResponseRva {\n");
		sb.append("    request: ").append(toIndentedString(request)).append("\n");
		sb.append("    answer: ").append(toIndentedString(answer)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces (except the first line).
	 */
	private String toIndentedString(Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
