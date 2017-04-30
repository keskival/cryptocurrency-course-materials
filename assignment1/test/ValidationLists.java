// Copyright (C) 2016-2017 Enrique Albertos
// Distributed under the GNU GPL v2 software license

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validation Lists is a container for several lists of elements calssified in:
 * Valid, Invalid or Conflicted
 * @author ealbertos
 *
 * @param <E> Type of the elements
 */
public class ValidationLists<E> {
	private final List<E> valid;
	private final List<E> invalid;
	private final List<E> conflicted;
	
	private ValidationLists(final List<E> valid, final List<E> invalid, final List<E> conflicted) {
		super();
		this.valid = Collections.unmodifiableList( new ArrayList<>(valid));
		this.invalid = Collections.unmodifiableList(new ArrayList<>(invalid));
		this.conflicted = Collections.unmodifiableList(new ArrayList<>(conflicted));
	}
	
	public ValidationLists(ValidationLists<E> original) {
		this(original.valid, original.invalid, original.conflicted);
	}
	
	public boolean isValid(final E e) {
		return valid.contains(e) && !invalid.contains(e);
	}
	
	public List<E> allElements() {
		final ArrayList<E> list = new ArrayList<>(valid);
		list.addAll(invalid);
		return list;
	}
	
	public static <T>  Builder<T> builder(Class<T> c){
		return new Builder<T>();
	}
	
	
	public static final class Builder<E>{
		private List<E> valid;
		private List<E> invalid;
		private List<E> conflicted;
		
		Builder<E> setValid(List<E> valid){
			this.valid = valid;
			return this;
		}
		
		Builder<E> setInvalid(List<E> invalid){
			this.invalid = invalid;
			return this;
		}
		
		Builder<E> setConflicted(List<E> conflicted){
			this.conflicted = conflicted;
			return this;
		}
		
		ValidationLists<E> build() {
			return new ValidationLists<>(valid, invalid, conflicted);
		}
		
	}
	

}
