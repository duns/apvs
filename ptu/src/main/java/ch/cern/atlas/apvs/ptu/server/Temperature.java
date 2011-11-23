package ch.cern.atlas.apvs.ptu.server;

import java.util.Date;

import ch.cern.atlas.apvs.domain.Measurement;

public class Temperature extends Measurement<Double> {

	public Temperature(int ptuId, double value) {
		super(ptuId, "Temperature", value, "&deg;C", new Date());
	}
	
}
