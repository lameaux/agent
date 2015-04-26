package com.euromoby.utils;

import java.util.List;

public class ListUtils {

	public static <T> T getFirst(List<T> list) {
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	public static <T> T getLast(List<T> list) {
		if (list.isEmpty()) {
			return null;
		}
		return list.get(list.size() - 1);
	}

}
