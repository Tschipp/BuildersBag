package tschipp.buildersbag.client.rendering;

public class BagRenderHelper
{

	// From https://easings.net/

	public static double easeOutCubic(double low, double high, double step)
	{
		return low + (1.0 - Math.pow(1.0 - step, 3)) * (high - low);
	}

	public static double easeInCubic(double low, double high, double step)
	{
		return low + (step * step * step) * (high - low);
	}

	public static double easeOutElastic(double low, double high, double step)
	{
		double c4 = (2 * Math.PI) / 3;
		double coeff = step == 0 ? 0 : step == 1 ? 1 : Math.pow(4.5, -8 * step) * Math.sin((step * 8 - 0.75) * c4) + 1;
		return low + (high - low) * coeff;
	}

	public static double easeOutBack(double low, double high, double step)
	{
		double c1 = 1.70158;
		double c3 = c1 + 1;
		double coeff = 1 + c3 * Math.pow(step - 1, 3) + c1 * Math.pow(step - 1, 2);
		return low + (high - low) * coeff;
	}

	public static double easeInOutBack(double low, double high, double step)
	{
		double c1 = 1.70158;
		double c2 = c1 * 1.525;
		double coeff = step < 0.5 ? (Math.pow(2 * step, 2) * ((c2 + 1) * 2 * step - c2)) / 2 : (Math.pow(2 * step - 2, 2) * ((c2 + 1) * (step * 2 - 2) + c2) + 2) / 2;
		return low + (high - low) * coeff;
	}

	public static double easeInExpo(double low, double high, double step)
	{
		double coeff = step == 0 ? 0 : Math.pow(2, 10 * step - 10);
		return low + (high - low) * coeff;
	}

	public static double easeOutBounce(double low, double high, double step)
	{
		double n1 = 7.5625;
		double d1 = 2.75;

		double coeff = 0;

		if (step < 1 / d1)
		{
			coeff = n1 * step * step;
		}
		else if (step < 2 / d1)
		{
			coeff = n1 * (step -= 1.5 / d1) * step + 0.75;
		}
		else if (step < 2.5 / d1)
		{
			coeff = n1 * (step -= 2.25 / d1) * step + 0.9375;
		}
		else
		{
			coeff = n1 * (step -= 2.625 / d1) * step + 0.984375;
		}
		return low + (high - low) * coeff;
	}

	public static double easeOutCirc(double low, double high, double step)
	{
		double coeff = step < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * step, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * step + 2, 2)) + 1) / 2;
		return low + (high - low) * coeff;
	}
}
