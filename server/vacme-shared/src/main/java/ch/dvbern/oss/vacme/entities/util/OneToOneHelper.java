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
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.checkerframework.checker.nullness.qual.Nullable;

public class OneToOneHelper<Parent, Child> implements Serializable {
	private static final long serialVersionUID = -4174773585305608995L;

	private final Supplier<Parent> parentSupplier;
	private final Supplier<@Nullable Child> parentGetChild;
	private final Consumer<Child> parentSetChild;

	private final Supplier<Child> childCtor;
	private final BiConsumer<Child, Parent> childSetParent;

	public OneToOneHelper(
			Supplier<Parent> parentSupplier,
			Supplier<@Nullable Child> parentGetChild,
			Consumer<Child> parentSetChild,
			Supplier<Child> childCtor,
			BiConsumer<Child, Parent> childSetParent
	) {
		this.parentSupplier = parentSupplier;
		this.parentGetChild = parentGetChild;
		this.parentSetChild = parentSetChild;
		this.childCtor = childCtor;
		this.childSetParent = childSetParent;
	}

	public Child createChild() {
		Child child = parentGetChild.get();
		if (child == null) {
			child = childCtor.get();
			Parent parent = parentSupplier.get();
			childSetParent.accept(child, parent);
			parentSetChild.accept(child);
		}

		return child;
	}

	public Child attach(Child child) {
		Objects.requireNonNull(child);

		Parent parent = parentSupplier.get();
		childSetParent.accept(child, parent);
		parentSetChild.accept(child);

		return child;
	}

	public boolean delete() {
		Child child = parentGetChild.get();
		boolean found = child != null;
		if (found) {
			childSetParent.accept(child, null);
		}
		parentSetChild.accept(null);

		return found;
	}
}
