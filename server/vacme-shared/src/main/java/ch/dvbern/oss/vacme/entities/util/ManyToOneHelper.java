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

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.base.IdSupplier;

public class ManyToOneHelper<Parent, Child extends IdSupplier<UUID>> implements Serializable {
	private static final long serialVersionUID = -4174773585305608995L;

	private final Supplier<Parent> parentSupplier;
	private final Supplier<Collection<Child>> parentGetChildren;

	private final Supplier<Child> childCtor;
	private final BiConsumer<Child, Parent> childSetParent;

	public ManyToOneHelper(
			Supplier<Parent> parentSupplier,
			Supplier<Collection<Child>> parentGetChildren,
			Supplier<Child> childCtor,
			BiConsumer<Child, Parent> childSetParent
	) {
		this.parentSupplier = parentSupplier;
		this.parentGetChildren = parentGetChildren;
		this.childCtor = childCtor;
		this.childSetParent = childSetParent;
	}

	public Child createChild() {
		Child child = childCtor.get();

		Child result = attachNewChild(child);

		return result;
	}

	public Child attachNewChild(Child child) {
		Parent parent = parentSupplier.get();
		childSetParent.accept(child, parent);
		parentGetChildren.get()
				.add(child);

		return child;
	}

	public Optional<Child> findById(UUID childId) {
		Collection<Child> children = parentGetChildren.get();
		return findById(children, childId);
	}

	private Optional<Child> findById(Collection<Child> children, UUID childId) {
		Optional<Child> result = children.stream()
				.filter(c -> childId.equals(c.getId()))
				.findAny();

		return result;
	}

	public boolean deleteChild(ID<Child> childId) {
		Collection<Child> children = parentGetChildren.get();

		Boolean result = findById(children, childId.getId())
				.map(c -> deleteChild(children, c))
				.orElse(false);

		return result;
	}

	private boolean deleteChild(Collection<Child> children, Child child) {
		childSetParent.accept(child, null);
		boolean result = children.remove(child);

		return result;
	}
}
