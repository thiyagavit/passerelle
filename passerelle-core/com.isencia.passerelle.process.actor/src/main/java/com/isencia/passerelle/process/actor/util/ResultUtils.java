package com.isencia.passerelle.process.actor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.isencia.passerelle.edm.engine.api.service.ServicesRegistry;
import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.NamedValue;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;

public final class ResultUtils {

	private ResultUtils() {
	};

	public static List<ResultItem> getAllItems(Context ctx) {
		List<Task> tasks = getAnalysisTasks(ctx.getRequest(), true, true);
		List<ResultItem> allItems = new ArrayList<ResultItem>();
		for (int i = 0; i < tasks.size(); ++i) {
			Task t = tasks.get(i);
			Collection<ResultBlock> blocks = t.getResultBlocks();
			Iterator<ResultBlock> blockItr = blocks.iterator();
			while (blockItr.hasNext()) {
				ResultBlock rb = blockItr.next();
				Iterator<ResultItem<?>> itemItr = rb.getAllItems().iterator();
				while (itemItr.hasNext()) {
					ResultItem item = itemItr.next();
					allItems.add(item);
				}
			}
		}
		return allItems;
	}

	public static List<NamedValue> getParameters(Request incomingRequest, boolean mostRecentOnly,
			boolean includeTransientTasks) {

		List<NamedValue> allParameters = new ArrayList<NamedValue>();
		if (includeTransientTasks) {
			Collection<Request> requests = incomingRequest.getCase().getRequests();
			for (Request request : requests) {
				addParametersFromRequest(request, allParameters);
			}
		} else {
			addParametersFromRequest(incomingRequest, allParameters);
		}

		return allParameters;
	}

	private static void addParametersFromRequest(Request request, List<NamedValue> allParameters) {
		if (request != null && request.getAttributes() != null) {
			Set<Attribute> parameters = request.getAttributes();
			allParameters.addAll(parameters);
		}
	}

	public static List<Task> getAnalysisTasks(Request incomingRequest, boolean mostRecentOnly,
			boolean includeTransientTasks) {
		Map<String, Task> mostRecentMap = new HashMap<String, Task>();

		List<Task> taskCopies = new ArrayList<Task>();

		if (includeTransientTasks) {
			Collection<Request> requests = incomingRequest.getCase().getRequests();
			for (Request request : requests) {
				addTasks(mostRecentMap, mostRecentOnly, taskCopies, request.getProcessingContext().getTasks());

			}
		} else {
			addTasks(mostRecentMap, mostRecentOnly, taskCopies, incomingRequest.getProcessingContext().getTasks());

		}

		return taskCopies;
	}

	public static void addTasks(Map<String, Task> mostRecentMap, boolean mostRecentOnly, List<Task> taskCopies,
			List<Task> tasks) {
		if (mostRecentOnly) {
			for (Task task : tasks) {
				if (task.getOwner() == null) {
					if (mostRecentMap.containsKey(task.getType())) {
						Task mostRecentTask = mostRecentMap.get(task.getType());
						if (task.getProcessingContext().getCreationTS().getTime() >= mostRecentTask
								.getProcessingContext().getCreationTS().getTime()) {
							mostRecentMap.put(mostRecentTask.getType(), task);
						}
					} else {
						mostRecentMap.put(task.getType(), task);
					}
				} else {
					if (mostRecentMap.containsKey(task.getOwner())) {
						Task mostRecentTask = mostRecentMap.get(task.getOwner());
						if (task.getProcessingContext().getCreationTS().getTime() >= mostRecentTask
								.getProcessingContext().getCreationTS().getTime()) {
							mostRecentMap.put(mostRecentTask.getOwner(), task);
						}
					} else {
						mostRecentMap.put(task.getType(), task);
					}
				}

			}
			taskCopies.addAll(mostRecentMap.values());
			Comparator<Task> comparator = new Comparator<Task>() {

				public int compare(Task arg0, Task arg1) {
					if (arg0.getProcessingContext().getCreationTS() != null
							&& arg1.getProcessingContext().getCreationTS() != null) {
						return arg1.getProcessingContext().getCreationTS()
								.compareTo(arg0.getProcessingContext().getCreationTS());
					}
					return 0;
				}

			};
			Collections.sort(taskCopies, comparator);

		} else {
			taskCopies.addAll(tasks);
		}
	}

	public static boolean checkMath(String operator, ResultItem ri1, ResultItem ri2, Object value) {
		Object result1 = ri1.getValue();
		Object result2 = ri2.getValue();
		if (value == null) {
			return false;
		}
		try {
			Double doubleValue = Double.parseDouble(value.toString());

			if (result1 instanceof Double && result2 instanceof Double) {
				Double doubleValue1 = (Double) ri1.getValue();
				Double doubleValue2 = (Double) ri2.getValue();
				if (operator.equals("abs diff >")) {
					return Math.abs(doubleValue1 - doubleValue2) > doubleValue;
				} else if (operator.equals("abs diff <")) {
					return Math.abs(doubleValue1 - doubleValue2) < doubleValue;
				} else if (operator.equals("abs diff ==")) {
					return Math.abs(doubleValue1 - doubleValue2) == doubleValue;
				} else if (operator.equals("abs diff <=")) {
					return Math.abs(doubleValue1 - doubleValue2) <= doubleValue;
				} else if (operator.equals("abs diff >=")) {
					return Math.abs(doubleValue1 - doubleValue2) >= doubleValue;
				} else {
					return false;
				}
			}
		} catch (Exception e) {

		}

		return false;
	}

	public static ResultItem addItem(ResultBlock results, String name, Object value) {

		if (value == null) {
			return null;
		}

		return ServicesRegistry.getInstance().getDiagnosisEntityFactory()
				.createResultItem(name, generateStringValue(value), "", results);
	}

	public static String generateStringValue(Object value) {
		String stringValue = null;
		if (value instanceof Collection) {
			Set<String> stringvalues = new HashSet<String>();
			for (Object val : (Collection) value) {
				stringvalues.add(generateStringValue(val));
			}
			stringValue = StringUtils.join(stringvalues, "|");
		} else if (value instanceof ResultItem) {
			stringValue = ((ResultItem) value).getValueAsString();

		} else {
			stringValue = value != null ? value.toString() : null;
		}
		return stringValue;
	}

	public static void groupItems(String name, Context context, List<ResultItem> items, int offSet) {
		context.putEntry(name, groupItems(name, (GroupItem) null, items, offSet));
	}

	public static ArrayList<GroupItem> groupItems(String name, GroupItem parent, List<ResultItem> items, int offSet) {
		ArrayList<GroupItem> groups = new ArrayList<GroupItem>();

		HashSet<String> prefixSet = new HashSet<String>();
		for (ResultItem ri : items) {
			String part = null;
			Pattern p = Pattern.compile("\\[\\d+\\]");
			String source = ri.getName();
			Matcher m = p.matcher(source);
			int occurenceCounter = offSet;
			while (m.find()) {
				if (occurenceCounter-- == 0) {
					part = source.substring(0, m.end());
				}

			}
			if (part != null) {
				prefixSet.add(part);
			}
		}
		if (prefixSet.isEmpty()) {
			return new ArrayList<GroupItem>();
		}
		offSet = offSet + 1;
		for (String prefix : prefixSet) {
			Map<String, ResultItem> domainMap = new HashMap<String, ResultItem>();
			Map<String, List<ResultItem>> children = new HashMap<String, List<ResultItem>>();
			for (ResultItem ri : items) {
				if (ri.getName().contains(prefix)) {
					int index = ri.getName().indexOf(prefix) + prefix.length() + 1;
					String substring = "this";
					if (index < ri.getName().length()) {
						substring = ri.getName().substring(index, ri.getName().length());
					}
					if (!substring.contains("[")) {
						domainMap.put(StringUtils.replaceChars(substring, ".", ""), ri);
					} else {
						String childName = StringUtils.split(substring, "[")[0];
						List<ResultItem> child = children.get(childName);
						if (child == null) {
							child = new ArrayList<ResultItem>();

							children.put(childName, child);
						}
						child.add(ri);
					}

				}

			}

			GroupItem group = createGroup(parent, name, domainMap);
			if (!children.keySet().isEmpty()) {
				for (Map.Entry<String, List<ResultItem>> entry : children.entrySet()) {
					List<GroupItem> groupItems = groupItems(entry.getKey(), group, entry.getValue(), offSet);
					group.addChildren(entry.getKey(), groupItems);
					group.put(entry.getKey(), groupItems);
				}
			}
			groups.add(group);
		}

		return groups;
	}

	public static GroupItem createGroup(GroupItem parent, String name, Map<String, ResultItem> map) {

		GroupItemImpl req = new GroupItemImpl(parent, name, map);

		return req;
	}

}
