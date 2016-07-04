package locationmanager.jgeraldo.com.androidlocationmanager.listeners;

/**
 * The listener interface for receiving onHomePressed events. The class that is
 * interested in processing a onHomePressed event implements this interface, and
 * the object created with that class is registered with a component using the
 * component's <code>addOnHomePressedListener</code> method. When
 * the onHomePressed event occurs, that object's appropriate
 * method is invoked.
 *
 * @see
 */
public interface OnHomePressedListener {

    void onHomePressed();

    void onHomeLongPressed();
}
