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

package ch.dvbern.oss.vacme.entities.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.dvbern.oss.vacme.entities.base.IdSupplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.vacme.entities.types.daterange.DateUtilTest.UUID_1;
import static ch.dvbern.oss.vacme.entities.types.daterange.DateUtilTest.UUID_2;
import static org.assertj.core.api.Assertions.assertThat;

class OneToManyHelperTest {

	@AllArgsConstructor
	@Getter
	public static class Parent {
		private final List<Child> children = new ArrayList<>();

		public OneToManyHelper<Parent, Child> helper() {
			return new OneToManyHelper<>(
					() -> this,
					this::getChildren,
					Child::new,
					Child::setParent
			);
		}

		public OneToManyHelper<Parent, Child> helperWithId(UUID childId) {
			return new OneToManyHelper<>(
					() -> this,
					this::getChildren,
					() -> {
						Child child = new Child();
						child.setId(childId);
						return child;
					},
					Child::setParent
			);
		}
	}

	@NoArgsConstructor
	@Getter
	@Setter
	public static class Child implements IdSupplier<UUID> {
		private UUID id = UUID.randomUUID();

		private Parent parent;
	}

	private final Parent parent = new Parent();

	@BeforeEach
	public void beforeEach() {

	}

	@Nested
	class CreateChildTest {
		private final UUID childId = UUID.fromString("00000000-0000-0000-0000-000000000000");
		private Child child = null;

		@BeforeEach
		public void beforeEach() {
			child = parent.helperWithId(childId).createChild();
		}

		@Test
		void should_create_child_with_correct_id() {
			assertThat(child.getId())
					.isEqualTo(childId);
		}

		@Test
		void should_create_child_with_correct_parent_reference() {
			assertThat(child.getParent())
					.isSameAs(parent);
		}

		@Test
		void should_add_child_to_list() {
			assertThat(parent.getChildren())
					.containsExactly(child);
		}

		@Test
		void should_add_more_children() {
			Child other = parent.helper().createChild();
			assertThat(parent.getChildren())
					.containsExactly(child, other);
		}
	}

	@Nested
	class RemoveTest {
		private final UUID childId1 = UUID_1;
		private final UUID childId2 = UUID_2;
		private Child child1 = null;
		private Child child2 = null;

		@Test
		void should_do_nothing_on_empty_children() {
			boolean removed = parent.helper().deleteChild(childId1);
			assertThat(removed)
					.isFalse();
		}

		@Nested
		class WithOneChildTest {

			@BeforeEach
			public void beforeEach() {
				child1 = parent.helperWithId(childId1).createChild();

				// sanity check
				assertThat(parent.getChildren())
						.contains(child1);
			}

			@Test
			void should_remove_the_one_child() {
				boolean removed = parent.helper().deleteChild(childId1);

				assertThat(removed)
						.isTrue();
			}

			@Test
			void should_contain_no_children_after_removal() {
				parent.helper().deleteChild(childId1);

				assertThat(parent.getChildren())
						.isEmpty();
			}
		}

		@Nested
		class WithMultipleChildenTest {
			@BeforeEach
			public void beforeEach() {
				child1 = parent.helperWithId(childId1).createChild();
				child2 = parent.helperWithId(childId2).createChild();
			}

			@Test
			void should_remove_child1() {
				boolean removed = parent.helper().deleteChild(child1);
				assertThat(removed)
						.isTrue();

				assertThat(parent.getChildren())
						.containsExactly(child2);

				assertThat(child1.getParent())
						.isEqualTo(parent);
			}

			@Test
			void should_remove_child2() {
				boolean removed = parent.helper().deleteChild(child2);
				assertThat(removed)
						.isTrue();

				assertThat(parent.getChildren())
						.containsExactly(child1);

				assertThat(child2.getParent())
						.isEqualTo(parent);
			}
		}

	}
}
