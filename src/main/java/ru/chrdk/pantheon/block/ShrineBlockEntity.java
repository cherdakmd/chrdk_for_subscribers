package ru.chrdk.pantheon.block;

/** Общий контракт «святилищ» чердака: постамент подписчика и портретная рама. */
public interface ShrineBlockEntity {
	/** Ник подписчика, чья фигурка (или портрет) здесь стоит; пустая строка — никто. */
	String getSubscriber();

	/** Тир подписчика (влияет на размер фигурки и пышность). */
	int getTier();

	void setSubscriber(String subscriber, int tier);
}
