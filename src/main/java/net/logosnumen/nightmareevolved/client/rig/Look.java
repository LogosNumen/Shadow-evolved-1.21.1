package net.logosnumen.nightmareevolved.client.rig;

/**
 * A creature form's colours: its hide, the darker shade and lighter sheen of it, its underside, its horns and claws,
 * the light that burns in its eyes, runes and veins, and a second accent.
 */
public record Look(int base, int dark, int light, int belly, int horn, int glow, int accent) {
}
