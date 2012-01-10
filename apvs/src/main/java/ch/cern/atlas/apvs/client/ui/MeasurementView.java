package ch.cern.atlas.apvs.client.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
import ch.cern.atlas.apvs.client.event.SupervisorSettingsChangedEvent;
import ch.cern.atlas.apvs.client.settings.Settings;
import ch.cern.atlas.apvs.client.settings.SupervisorSettings;
import ch.cern.atlas.apvs.client.widget.VerticalFlowPanel;
import ch.cern.atlas.apvs.domain.Measurement;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class MeasurementView extends VerticalFlowPanel {

	private static NumberFormat format = NumberFormat.getFormat("0.00");

	private SupervisorSettings settings;
	private Measurement<Double> last = new Measurement<Double>();
	private ListDataProvider<Measurement<Double>> dataProvider = new ListDataProvider<Measurement<Double>>();
	private CellTable<Measurement<Double>> table = new CellTable<Measurement<Double>>();
	private ListHandler<Measurement<Double>> columnSortHandler;
	private TextColumn<Measurement<Double>> name;

	private Integer ptuId = null;

	public MeasurementView(final RemoteEventBus remoteEventBus, final RemoteEventBus localEventBus) {

		add(table);
		
		SupervisorSettingsChangedEvent.subscribe(remoteEventBus, new SupervisorSettingsChangedEvent.Handler() {
			
			@Override
			public void onSupervisorSettingsChanged(SupervisorSettingsChangedEvent event) {
				settings = event.getSupervisorSettings();
				
				update();
			}
		});

		SelectPtuEvent.register(localEventBus, new SelectPtuEvent.Handler() {

			private HandlerRegistration registration;

			@Override
			public void onPtuSelected(final SelectPtuEvent event) {
				if (!localEventBus.getUUID().equals(event.getEventBusUUID()))
					return;

				// unregister any remaining handler
				if (registration != null) {
					registration.removeHandler();
					registration = null;
				}

				ptuId = event.getPtuId();

				// register a new handler
				if (ptuId != null) {
					registration = MeasurementChangedEvent.subscribe(remoteEventBus,
							new MeasurementChangedEvent.Handler() {

								@Override
								public void onMeasurementChanged(
										MeasurementChangedEvent event) {
									Measurement<Double> measurement = event
											.getMeasurement();
									if (measurement.getPtuId() == ptuId) {
										last = replace(measurement);
										update();
									}
								}
							});
				} else {
					dataProvider.getList().clear();
				}
				
				update();
			}
		});

		name = new TextColumn<Measurement<Double>>() {
			@Override
			public String getValue(Measurement<Double> object) {
				return object.getName();
			}

			@Override
			public void render(Context context, Measurement<Double> object,
					SafeHtmlBuilder sb) {
				String name = getValue(object);
				if (name != null) {
					((TextCell) getCell()).render(context,
							SafeHtmlUtils.fromSafeConstant(name), sb);
				}
			}
		};
		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		name.setSortable(true);
		table.addColumn(name, new TextHeader("") {
			@Override
			public String getValue() {
				if (ptuId == null) return "Name";
				
				if (settings != null) {
					String name = settings.getName(Settings.DEFAULT_SUPERVISOR, ptuId);
					
					if (name != null) return name;
				}
				
				return "PTU Id: "+ptuId;
			}			
		});

		TextColumn<Measurement<Double>> value = new TextColumn<Measurement<Double>>() {
			@Override
			public String getValue(Measurement<Double> object) {
				if (object == null) {
					return "";
				}
				return format.format(object.getValue());
			}

			@Override
			public void render(Context context, Measurement<Double> object,
					SafeHtmlBuilder sb) {
				String s = getValue(object);
				// FIXME does not work for dosimeter
				((TextCell) getCell()).render(context,
						decorate(s, object, last), sb);
			}

		};
		value.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		table.addColumn(value, "Value");

		TextColumn<Measurement<Double>> unit = new TextColumn<Measurement<Double>>() {
			@Override
			public String getValue(Measurement<Double> object) {
				return object.getUnit();
			}

			@Override
			public void render(Context context, Measurement<Double> object,
					SafeHtmlBuilder sb) {
				String unit = getValue(object);
				if (unit != null) {
					((TextCell) getCell()).render(context,
							SafeHtmlUtils.fromSafeConstant(unit), sb);
				}
			}
		};
		unit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		unit.setSortable(true);
		table.addColumn(unit, "Unit");

		List<Measurement<Double>> list = new ArrayList<Measurement<Double>>();
		dataProvider.addDataDisplay(table);
		dataProvider.setList(list);

		columnSortHandler = new ListHandler<Measurement<Double>>(
				dataProvider.getList());
		columnSortHandler.setComparator(name,
				new Comparator<Measurement<Double>>() {
					public int compare(Measurement<Double> o1,
							Measurement<Double> o2) {
						if (o1 == o2) {
							return 0;
						}

						if (o1 != null) {
							return (o2 != null) ? o1.getName().compareTo(
									o2.getName()) : 1;
						}
						return -1;
					}
				});
		columnSortHandler.setComparator(value,
				new Comparator<Measurement<Double>>() {
					public int compare(Measurement<Double> o1,
							Measurement<Double> o2) {
						if (o1 == o2) {
							return 0;
						}

						if (o1 != null) {
							return (o2 != null) ? o1.getValue().compareTo(
									o2.getValue()) : 1;
						}
						return -1;
					}
				});
		columnSortHandler.setComparator(unit,
				new Comparator<Measurement<Double>>() {
					public int compare(Measurement<Double> o1,
							Measurement<Double> o2) {
						if (o1 == o2) {
							return 0;
						}

						if (o1 != null) {
							return (o2 != null) ? o1.getUnit().compareTo(
									o2.getUnit()) : 1;
						}
						return -1;
					}
				});
		table.addColumnSortHandler(columnSortHandler);
		table.getColumnSortList().push(name);
	}

	public static SafeHtml decorate(String s, Measurement<Double> current,
			Measurement<Double> last) {
		if ((current != null) && (last != null)
				&& (current.getPtuId() == last.getPtuId())
				&& current.getName().equals(last.getName())) {
			double c = current.getValue();
			double l = last.getValue();
			String a = (c == l) ? "&larr;" : (c > l) ? "&uarr;" : "&darr;";
			s = a + "&nbsp;<b>" + s + "</b>";
		}
		return SafeHtmlUtils.fromSafeConstant(s);
	}

	private void update() {
		// Re-sort the table
		ColumnSortEvent.fire(table, table.getColumnSortList());
		table.redraw();
	}

	private Measurement<Double> replace(Measurement<Double> measurement) {
		List<Measurement<Double>> list = dataProvider.getList();

		int i = 0;
		while (i < list.size()) {
			if (list.get(i).getName().equals(measurement.getName())) {
				break;
			}
			i++;
		}

		Measurement<Double> lastValue;
		if (i == list.size()) {
			list.add(measurement);
			lastValue = measurement;
		} else {
			lastValue = list.set(i, measurement);
		}

		return lastValue;
	}

}