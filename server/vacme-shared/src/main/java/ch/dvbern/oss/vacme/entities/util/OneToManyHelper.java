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
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import ch.dvbern.oss.vacme.entities.base.ID;
import ch.dvbern.oss.vacme.entities.base.IdSupplier;

@SuppressWarnings({ "ProtectedField", "CdiInjectionPointsInspection" })
public class OneToManyHelper<Parent, Child extends IdSupplier<UUID>> implements Serializable {
	private static final long serialVersionUID = -4174773585305608995L;

	protected final Supplier<Parent> parentSupplier;
	protected final Supplier<Collection<Child>> parentGetChildren;

	protected final Supplier<Child> childCtor;
	protected final BiConsumer<Child, Parent> childSetParent;

	public OneToManyHelper(
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

	public boolean deleteChild(Child child) {
		return deleteChild(child.getId());
	}

	public boolean deleteChild(UUID childId) {
		return deleteChildren(child -> child.getId().equals(childId));
	}

	public boolean deleteChildren(Predicate<Child> predicate) {
		Collection<Child> children = parentGetChildren.get();

		boolean found = false;
		for (Iterator<Child> iter = children.iterator(); iter.hasNext(); ) {
			Child c = iter.next();
			if (predicate.test(c)) {
				//FYI: If there is a @NotNull annotation on the childs parent relation (which it should usually be),
				// beanvalidation will refuse to persist the entity if we set child.parent to null.
				// => do not set child.parent to null :(
				//childSetParent.accept(c, null);
				iter.remove();
				found = true;
			}
		}

		return found;
	}

	public void clear() {
		//FYI: If there is a @NotNull annotation on the childs parent relation (which it should usually be),
		// beanvalidation will refuse to persist the entity if we set child.parent to null.
		// => do not set child.parent to null :(
		parentGetChildren.get().clear();
	}

	/**
	 * Find a child in the collection of children.
	 */
	public Optional<Child> findChild(Predicate<Child> predicate) {
		Optional<Child> result = parentGetChildren.get()
				.stream()
				.filter(predicate)
				.findAny();

		return result;
	}

	/**
	 * Convenience: find a child by its id.
	 * See {@link #findChild(Predicate)} for details.
	 */
	public Optional<Child> findChild(UUID childId) {
		return findChild(c -> childId.equals(c.getId()));
	}

	/**
	 * Convenience, see {@link #findChild(UUID)}
	 */
	public Optional<Child> findChild(ID<Child> childId) {
		return findChild(c -> childId.getId().equals(c.getId()));
	}

}
