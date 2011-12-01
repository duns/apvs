package ch.cern.atlas.apvs.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
import ch.cern.atlas.apvs.client.service.PtuServiceAsync;
import ch.cern.atlas.apvs.eventbus.shared.SimpleRemoteEventBus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;

public class PtuSelector extends SimplePanel {

	private ListBox list = new ListBox();

	private PtuServiceAsync ptuService;
	private List<Integer> ptuIds = new ArrayList<Integer>();

	public PtuSelector(final SimpleRemoteEventBus eventBus,
			PtuServiceAsync ptuService) {
		this.ptuService = ptuService;

		add(list);

		list.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int ptuId;
				try {
					ptuId = Integer.parseInt(list.getItemText(list
							.getSelectedIndex()));
				} catch (NumberFormatException e) {
					ptuId = 0;
				}
				eventBus.fireEvent(new SelectPtuEvent(ptuId));
			}
		});

		SelectPtuEvent.register(eventBus, new SelectPtuEvent.Handler() {

			@Override
			public void onPtuSelected(SelectPtuEvent event) {
				for (int i = 0; i < list.getItemCount(); i++) {
					if (list.getValue(i).equals(toLabel(event.getPtuId()))) {
						list.setSelectedIndex(i);
						break;
					}
				}
			}
		});

		getPtuIds();
	}

	private void getPtuIds() {
		ptuService.getPtuIds((long) ptuIds.hashCode(),
				new AsyncCallback<List<Integer>>() {

					@Override
					public void onSuccess(List<Integer> result) {
						if (result == null) {
							System.err
									.println("FIXME onSuccess null in ptuSelector");
							return;
						}
						ptuIds = result;

						list.clear();
						list.addItem("Select PTU ID");
						for (Iterator<Integer> i = ptuIds.iterator(); i
								.hasNext();) {
							list.addItem(toLabel(i.next()));
						}

						getPtuIds();
					}

					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Could not retrieve dosimeterSerialNumbers");

						getPtuIds();
					}
				});
	}

	private String toLabel(int ptuId) {
		return Integer.toString(ptuId);
	}
}