/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.vacme.shared.errors.mappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Path.Node;

import ch.dvbern.oss.vacme.shared.errors.FailureType;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

@Getter
@Slf4j
public class AppValidationErrorResponse extends AppErrorResponse {

	private final List<Violation> violations;

	public AppValidationErrorResponse(Collection<ConstraintViolation<?>> violations) {
		super(FailureType.VALIDATION);

		this.violations = violations.stream()
				.map(Violation::new)
				.collect(Collectors.toList());
	}

	public AppValidationErrorResponse(
			String message,
			String key,
			List<Reference> referenceList
	) {
		super(FailureType.VALIDATION);

		// just return one violation, since its from a exception
		violations = List.of(
				new Violation(buildPath(referenceList), key, message, null)
		);
	}

	@Getter
	@ToString
	@AllArgsConstructor
	public static class Violation {

		private final String path;
		/**
		 * Mainly used for locale-independent testing
		 */
		private final String key;
		private final String message;

		private final String rootBean;

		public Violation(ConstraintViolation<?> constraintViolation) {
			path = buildPath(constraintViolation);
			key = constraintViolation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
			message = constraintViolation.getMessage();
			rootBean = constraintViolation.getRootBean() != null ? constraintViolation.getRootBean().getClass().getSimpleName() : null;
		}

		// TODO: REST Error Response verbessern
		// die Methode muss raus aus Violation, da sie nicht nur fuer AppValidation gebrauchtg wird
		// sondern auch fuer AppFailures. Failures werden hier aber nur mit einer RuntimeException behandelt
		// und werden damit recht unschoen ans GUI weitergeleitet.
		// In der Endausbaustufe sollte Methode wohl andhand der Teile im Path entscheiden,
		// ob eine ValidationErrorResponse oder AppFailureResponse gebaut wird.
		private String buildPath(ConstraintViolation<?> constraintViolation) {
			Path propertyPath = constraintViolation.getPropertyPath();
			Iterator<Node> iterator = propertyPath.iterator();

			if (!iterator.hasNext()) {
				throw new RuntimeException(
						String.format("Not yet implemented: violations without path %s, %s",
								propertyPath, constraintViolation));
			}

			Node first = iterator.next();
			switch (first.getKind()) {
			case BEAN:
				if (!iterator.hasNext()) {
					return constraintViolation.getLeafBean().getClass().getSimpleName();
				}
				throw new NotImplementedException("BEAN");
			case PROPERTY:
				return buildNodePath(asList(propertyPath));
			case PARAMETER:
			case METHOD:
				// case CONSTRUCTOR:
				// case CROSS_PARAMETER:
				// case RETURN_VALUE:
				// case CONTAINER_ELEMENT:
				List<Node> nodes = asList(propertyPath);
				List<Node> justParam = nodes.subList(2, nodes.size());

				return buildNodePath(justParam);
			default:
				throw new RuntimeException(String.format(
						"Not yet implemented validation exception for type %s, %s",
						first.getKind(), constraintViolation));
			}
		}

		private String buildNodePath(List<Node> path) {
			// example:
			// parent.child[1].property
			// => entries:
			//    parent (isInIterable=false,index=null)
			//    child  (isInIterable=false,index=null)
			//    property (isInIterable=true,index=1);

			var pathEntries = new ArrayList<String>();
			for (int i = 0; i < path.size(); i++) {
				Node next = path.get(i);

				if (next.getKey() != null) {
					throw new NotImplementedException("Path with map keys is not implemented: " + path.toString());
				}

				pathEntries.add(i, next.getName());

				if (next.isInIterable()) {
					var prevIdx = i - 1;
					String previous = pathEntries.get(prevIdx);
					String prevIndexed = previous + '[' + next.getIndex() + ']';
					pathEntries.set(prevIdx, prevIndexed);
				}
			}

			var result = String.join(".", pathEntries);

			return result;
		}

		private static List<Node> asList(Path propertyPath) {
			var result = StreamSupport.stream(propertyPath.spliterator(), false)
					.collect(Collectors.toList());

			return result;
		}
	}
}
